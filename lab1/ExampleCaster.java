
import mcgui.*;
import java.util.ArrayList;

/**
 * Simple example of how to use the Multicaster interface.
 * 
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 * @author Erland;
 * @author Mange;
 */
public class ExampleCaster extends Multicaster {

    // locical clock of each process and a buffer used to guarantee FIFO per each process
    // TODO: might have to redo some of this as the format of all messages are differnet now, probably need to save more data so to also achive total order
    private int[] localSeqArray; 
    private ArrayList<ExampleMessage> buffer;

    // this works as a int tuple, where the first value is logical clock-value/sequence number and the second is id of proposer
    private int globalSeq; // aka "A"
    private int globalID;
    private int proposeSeq; // aka "P"
    private int proposeID; 

    public void init() {
        mcui.debug("The network has " + hosts + " hosts!");
        localSeqArray = new int[hosts];
        buffer = new ArrayList<ExampleMessage>();

        globalSeq = 0;
        globalID = 0;
        
        proposeSeq = 0;
        proposeID = 0;

    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
        ExampleMessage eMessage;
        // phase 1 -- requesting clockvalues, sening the msg and a unique identifier (id, potential local clock vlaue) NOTE: logic of local clock value might be worng
        eMessage = new ExampleMessage("req", messagetext, id , localSeqArray[id]+1);
        send(eMessage);
    }

    /**
     * Actual method that broadcasts messages
     * @param message  The message received
     */
    private void send(Message message) {
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, message);
            }
        }
        mcui.debug("Sent out: \""+message.getText()+"\"");
        mcui.deliver(id, message.getText(), "from myself!");
    }




    /* THIS DIFFERS FROM LAST COURSE IN THE WAY THAT WE HAVE TO CONSIDER HOW TO SEND THE MESSAGES AS WELL AS THE DS ALGO. DID NOT EVEN NOTICE THIS... MAYBE BECAUSE I WE ARE GETTING SO USED TO DS? */




    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer,Message message) {
        ExampleMessage eMessage = (ExampleMessage)message;

        //TODO: implement Reliable-broadcast!!!

        // PHASE 1 -- recives request for proposal and message, sending propsal
        if (eMessage.getStatus().equals("req")) {
            tmp = Math.max(proposeSeq, globalSeq) + 1;
            if ((tmp - 1) == globalSeq) { // was global biggest?
                proposeSeq = tmp;
                proposeID = tmp;
            }
            eMessage = new ExampleMessage("pro", proposeSeq, proposeID);
        
        // PHASE 2 -- recives proposal, broadcasting the new message with new correct clock-value
        // NOTE: does not send the message again, only the unique msg identifier to find the msg in some buffer and what global sequence it should have
        } else if (eMessage.getStatus().equals("pro"))) {
            if (proposalArray.get().size() == hosts) {
                int tmpSeq;
                int tmpID;
                for (ExampleMessage eMessage : proposalArray.get()) { 
                    if (eMessage.getLocal
                }
                localSeqArray[id] ++;
                eMessage = ExampleMessage("seq", id , localSeqArray[id], globalSeq, globalID);
                send(eMessage);
            } else {
                proposalArray.get().add(eMessage)
            }
            

        // PHASE 3 -- recives global clovk value/sequence for a message, i.e. actually deliver msg
        } else if (eMessage.getStatus().equals("seq")) {
            //code
            

        // else error
        } else {
            mcui.debug("Error - basicrecive() failed to find right phase");
            
        }





        if (eMessage.getVectorValue() != localSeqArray[eMessage.getSender()] + 1 ) {

            // save message to a buffer (one for each sender), this is done if messages are not deleiverd in correct order
            buffer.add(eMessage); 
            //mcui.debug("value added TO buffer");
        
        } else {

            // this if-statement is not really necessary as it would be okey to just overwite the correct value in the array with the same value
            if (eMessage.getSender() != id) { 
                localSeqArray[eMessage.getSender()] = eMessage.getVectorValue();
            } 
            mcui.deliver(peer, eMessage.getText());

            // check if buffer messages can be added 
            checkBuffer(eMessage);

        }
    }

    private void checkBuffer(ExampleMessage latestMsg){
        for (ExampleMessage bufferMessage : buffer) {
            if (bufferMessage.getSender() == latestMsg.getSender() && bufferMessage.getVectorValue() == localSeqArray[latestMsg.getSender()] + 1) {
                localSeqArray[bufferMessage.getSender()] = bufferMessage.getVectorValue();
                mcui.debug("Added value FROM buffer");
                checkBuffer(bufferMessage);

            }
        }
    }

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
