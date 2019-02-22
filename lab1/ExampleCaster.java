
import mcgui.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Simple example of how to use the Multicaster interface.
 * 
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 * @author Erland;
 * @author Mange;
 */
public class ExampleCaster extends Multicaster {

    // locical clock of each process and a buffer used to guarantee FIFO per each process
    // TODO-mid-prio: might have to redo some of this as the format of all messages are differnet now, probably need to save more data so to also achive total order
    private int[] localSeqArray; 
    private ArrayList<ArrayList<ExampleMessage>> buffer;
    private ArrayList<ExampleMessage> proposalArray;

    // this works as a int tuple, where the first value is logical clock-value/sequence number and the second is id of proposer
    private int globalSeq; // aka "A"
    private int globalID;
    private int proposeSeq; // aka "P"
    private int proposeID; 

    

    public void init() {
        mcui.debug("The network has " + hosts + " hosts!");
        localSeqArray = new int[hosts];
        buffer = new ArrayList<ArrayList<ExampleMessage>>(hosts);
        for (int i = 0; i < hosts; i++) {
            buffer.add(new ArrayList<ExampleMessage>());
        }
        proposalArray = new ArrayList<ExampleMessage>();

        globalSeq = 0;
        globalID = 0;
        
        proposeSeq = 0;
        proposeID = 0;

    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {

        //TODO: buffer upp messages being sent until curnet one that is waiting for propsalsas is finished?!?!?!


        // phase 1 -- requesting clockvalues, sening the msg and a unique identifier (id, potential local clock vlaue) NOTE: logic of local clock value might be worng
        ExampleMessage eMessage = new ExampleMessage("req", messagetext, id , localSeqArray[id]+1);
        send(eMessage);
    }

    /**
     * Actual method that broadcasts messages
     * @param message  The message received
     */
    private void send(Message message) {
        ExampleMessage eMessage = (ExampleMessage) message;
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, eMessage);
            }
            //bcom.basicsend(i, eMessage);
        }
        //mcui.debug("Sent out: \""+eMessage.getText()+"\"");
        //mcui.deliver(id, eMessage.getText(), "from myself!");
    }




    /* THIS DIFFERS FROM LAST COURSE IN THE WAY THAT WE HAVE TO CONSIDER HOW TO SEND THE MESSAGES AS WELL AS THE DS ALGO. DID NOT EVEN NOTICE THIS... MAYBE BECAUSE I WE ARE GETTING SO USED TO DS? */




    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer,Message message) {    //TODO-low-prio: implement Reliable-broadcast!!!
        ExampleMessage eMessage = (ExampleMessage)message;

        // PHASE 1 -- recives request for proposal and message, sending propsal
        if (eMessage.getStatus().equals("req")) {
            phase1(eMessage);
        
        // PHASE 2 -- recives proposal, broadcasting the new message with new correct clock-value
        // NOTE: does not send the message again, only the unique msg identifier to find the msg in some buffer and what global sequence it should have
        } else if (eMessage.getStatus().equals("pro")) {
            phase2(eMessage);
            

        // PHASE 3 -- recives global clovk value/sequence for a message, i.e. actually deliver msg
        } else if (eMessage.getStatus().equals("seq")) {
            phase3(eMessage);


        // else error
        } else {
            mcui.debug("Error - basicrecive() failed to find right phase");   
        }
    }

    // PHASE 1 -- recives request for proposal and message; sending propsal and saving message. message is maped ot sender and sender local sequence
    public void phase1(ExampleMessage eMessage) {
        
        // save all text from recived messages, mapped to sender and sender local sequence
        if (!buffer.get(eMessage.getSender()).contains(eMessage)) {
            buffer.get(eMessage.getSender()).add(eMessage);
        }
        
        int tmp = Math.max(proposeSeq, globalSeq) + 1;
        if ((tmp - 1) == globalSeq) { // was global biggest?
            proposeSeq = tmp;
            proposeID = tmp;
        }
        send( new ExampleMessage("pro", proposeSeq, proposeID));
    }

    // PHASE 2 -- recives proposal, broadcasting the new message with new correct clock-value
    public void phase2(ExampleMessage eMessage) {
        if (proposalArray.size() != hosts - 1) {
            proposalArray.add(eMessage);
            //todo: others had added in here aswell...
        } 
        if (proposalArray.size() == (hosts - 1)) {

            System.out.println("asd");
            int tmpSeq = 0;
            int tmpID = 0;
            for (ExampleMessage eMsg : proposalArray) { 
                if (eMsg.getProposeSeq() == tmpSeq && eMsg.getProposeID() > tmpID) {
                    tmpID = eMsg.getProposeID();
                } else if (eMsg.getProposeSeq() > tmpSeq){
                    tmpSeq = eMsg.getProposeSeq();
                    tmpID = eMsg.getProposeID();
                }
            }
            globalSeq = tmpSeq;
            globalID = tmpID;
            localSeqArray[id] ++;
            send( new ExampleMessage("seq", id , localSeqArray[id], globalSeq, globalID));
            proposalArray.clear(); // this array is reused for each new message from this process, this is why we can not handle more than one message cast() at the time 
            // TODO-low-prio: fix wo that a process can hanlde multipe of its own message proposals at the time 
        }
    }

    // PHASE 3 -- recives global clovk value/sequence for a message, i.e. actually deliver msg
    public void phase3(ExampleMessage eMessage) {
        
        System.out.println("eMessage.getLocalID()   --- " + eMessage.getLocalID());
        System.out.println("buffer.get(eMessage.getLocalID()).getLocalSeq() ---- " +buffer.get(eMessage.getLocalID()).getLocalSeq());
        
        // find the correct message from buffer where text of all messages are saved
        for (ExampleMessage eMsg : buffer.get(eMessage.getLocalID())) {
            System.out.println("2");
            if (eMsg.getLocalSeq() == eMessage.getLocalSeq() ) { //check FIFO: && eMsg.getLocalSeq()

                System.out.println("3");
//                System.out.println("eMsg.getSender() : " + eMsg.getSender() + "  --  eMsg.getText(): " + eMsg.getText() );
                
                // TODO: when to deliver, buffer?
                mcui.deliver(eMsg.getSender(), eMsg.getText());
            }
        }
        

        // check local FIFO:
        
    }
/*
    private void checkBuffer(ExampleMessage latestMsg){
        for (ExampleMessage bufferMessage : buffer) {
            if (bufferMessage.getSender() == latestMsg.getSender() && bufferMessage.getSequence() == localSeqArray[latestMsg.getSender()] + 1) {
                localSeqArray[bufferMessage.getSender()] = bufferMessage.getSequence();
                mcui.debug("Added value FROM buffer");
                checkBuffer(bufferMessage);

            }
        }
    }
*/
    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
    }
}
