
import mcgui.*;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 * 
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 * @author Erland;
 * @author Mange;
 */
public class ExampleCaster extends Multicaster {
    
    private int globalSequence = 0;
    private int globalID = 0;
    private int localSequence = 0;
    private int p = 0;
    private int a = 0;

    private ArrayList<ArrayList<ExampleMessage>> proposalBuffer;

    private ArrayList<ArrayList<ExampleMessage>> backlog;
    private ArrayList<ExampleMessage> backlogBuffer;

    public void init() {
        mcui.debug("The network has " + hosts + " hosts!");
        proposalBuffer = new ArrayList<ArrayList<ExampleMessage>>();
        //for (int i = 0; i < hosts; i ++) {
            //proposalBuffer.add(new ArrayList<ExampleMessage>());
        //}
        
        backlog = new ArrayList<ArrayList<ExampleMessage>>();
        backlogBuffer = new ArrayList<ExampleMessage>();
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {

        // phase 1 Sends Requesst for Proposal -- requesting clockvalues, sening the msg and a unique identifier (id, potential local clock vlaue) NOTE: logic of local clock value might be worng
        ExampleMessage eMessage = new ExampleMessage("req", messagetext, id, id, localSequence, globalSequence, false);
        add2Backlog(eMessage);
        send(eMessage);
        
        // This is last so as to utilize the initial value 0.
        localSequence ++;
    }

    /**
     * Actual method that broadcasts messages
     * @param message  The message received
     */
    private void send(ExampleMessage message) {
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i, message);
            }
        }
        //mcui.debug("Sent out: \""+eMessage.getText()+"\"");
        //mcui.deliver(id, eMessage.getText(), "from myself!");
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer, Message message) {    //TODO-low-prio: implement Reliable-broadcast!!!
        ExampleMessage eMessage = (ExampleMessage)message;

        // PHASE 2 Recives Proposal -- recives proposal, broadcasting the new message with new correct clock-value
        if (eMessage.getStatus().equals("pro")) {
            phase2RP(eMessage);

        // PHASE 2 Recives Requesst for Proposal -- recives request for proposal and message, sending propsal
        } else if (eMessage.getStatus().equals("req")) {
            phase2RRFP(eMessage);
            
        // PHASE 3 -- recives global clovk value/sequence for a message, i.e. actually deliver msg
        } else if (eMessage.getStatus().equals("fin")) {
            phase3(eMessage);

        // else error
        } else {
            mcui.debug("Error - basicrecive() failed to find right phase");   
        }
    }

    // PHASE 2 Recives Proposal -- recives proposal, broadcasting the new message with new correct clock-value
    public void phase2RP(ExampleMessage eMessage) {

        // WTF is this!?!?!?!
        //int index = eMessage.getLocalSequence() % hosts; 


        //  ========================== TODO: start debugging here? ==========================

        try {
            proposalBuffer.get(eMessage.getLocalSequence());
        } catch (Exception e) {
            proposalBuffer.add(new ArrayList<ExampleMessage>());
        } 

        if (!proposalBuffer.get(eMessage.getLocalSequence()).contains(eMessage)) {
            proposalBuffer.get(eMessage.getLocalSequence()).add(eMessage);
        }
        
        // checks if all proposals are in
        // TODO: fix hosts-check to cope with dead nodes
        if (proposalBuffer.get(eMessage.getLocalSequence()).size() == hosts - 1) {
            int propMax = 0; 
            int propID = 0;
            for (ExampleMessage tmp : proposalBuffer.get(eMessage.getLocalSequence())) {
                if (tmp.getGlobalSequence() > propMax) { 
                    propMax = tmp.getGlobalSequence();        // tmp.getSender() proposese tmp.getSequence()
                    propID = tmp.getProposerID();
                }
            }
            globalSequence = propMax; 
            globalID = propID; //TODO: not need?
            //System.out.println("I AM " + id + " ----  my txt should be same as you wronte in my client: " +eMessage.getText());

            // constructs messages that is deliverable and has final global seq
            ExampleMessage answer = new ExampleMessage("fin", eMessage.getText(), eMessage.getSender(), propID, eMessage.getLocalSequence(), propMax, true);    // TODO: test if "id" can be changed to "propID"
            
            add2Backlog(answer);
            send(answer); // TODO: r-boradcast this? 

            proposalBuffer.get(eMessage.getLocalSequence()).clear();
        } 
    }

    // PHASE 2 Recives Requesst for Proposal -- recives request for proposal and message; sending propsal and saving message. message is maped ot sender and sender local sequence
    public void phase2RRFP(ExampleMessage eMessage) {
        p = Math.max(a, p) + 1;
        ExampleMessage answer = new ExampleMessage("pro", eMessage.getText(), eMessage.getSender(), id, eMessage.getLocalSequence(), p, false);
        bcom.basicsend(eMessage.getSender(), answer); // TODO: r-boradcast this? use send?
        
        add2Backlog(answer);
        
        //System.out.println("----- my node id: " + id + " --- --- answer.id:  " + answer.getSender() + "  --- --- eMessage.id : " + eMessage.getSender() );

    }

    // PHASE 3 -- recives final clovk value/sequence for a message, i.e. actually deliver msg
    public void phase3(ExampleMessage eMessage) {
        //System.out.println("I AM " + id + " ----  I just recived a final deliver on text:  " + eMessage.getText() + " ---- from: " + eMessage.getSender() + " ---- proposed id: " + eMessage.getSequence());
        a = Math.max(eMessage.getGlobalSequence(), a);

        add2Backlog(eMessage); 
    }

    public void add2Backlog(ExampleMessage eMessage) {

        Boolean breakBool = false;

        checkBacklog(eMessage);

        // Assumes FIFO
        for (ArrayList<ExampleMessage> al : backlog) {
            for (ExampleMessage eMsg : al) {

                // check if messeage is allready in backlog, if so always write over with newer message
                if (eMsg.getSender() != -1 && eMsg.getSender() == eMessage.getSender() && eMsg.getLocalSequence() == eMessage.getLocalSequence()) {
                    


                    // remove message at old position
                    al.set(eMsg.getProposerID(), new ExampleMessage(-1));

                    // override with new message
                    backlog.get(eMessage.getGlobalSequence()).set(eMessage.getProposerID(), eMessage);
                    
                    breakBool = true;
                    break;
                }
            }
            if (breakBool) { break; } 
        }

        // message was not in backlog
        if (!breakBool) {
            
            // override with new message
            backlog.get(eMessage.getGlobalSequence()).set(eMessage.getProposerID(), eMessage);
        }
        deliver();
    }

    public void checkBacklog(ExampleMessage eMessage) {
        
        // initiate all necessary ArrayLists/X
        for (int j = 0; j <= eMessage.getGlobalSequence(); j ++ ) {
            
            // check if the "row" (array list) allready excists
            try {
                backlog.get(j);
            } catch (Exception e) {
                backlog.add(j, new ArrayList<ExampleMessage>());
            }
        }

        // initiate all necessary insider-ArrayLists/Y
        for (int i = 0; i <= eMessage.getProposerID(); i ++) {
            
            // check if message at index i is initiated 
            try {
                backlog.get(eMessage.getGlobalSequence()).get(i);
            } catch (Exception e) {
                backlog.get(eMessage.getGlobalSequence()).add(i, new ExampleMessage(-1));
            }
        }
    }

    public void deliver() {

        Boolean breakBool = false;

        for (ArrayList<ExampleMessage> al : backlog) {
            for (ExampleMessage eMsg : al) {
                
                //System.out.println("I AM : " + id + " --- is eMsg null? " + eMsg + " --- eMsg.deliverable ? " + eMsg.getDeliverable());

                // finds first avalable value
                if (eMsg.getSender() != -1) {
                    if (eMsg.getDeliverable() == true) {

                        mcui.deliver(eMsg.getSender(), eMsg.getText());

                        // TODO: this is not nice code... leaves the Arraylist filled with null-objects instead of empty...
                        al.remove(eMsg.getProposerID());
                        // to not destroy the indexing
                        al.add(eMsg.getProposerID(), new ExampleMessage(-1));
                        
                        // recursivly call method till next !deliverable is found
                        deliver();

                        
                    } 
                    breakBool  = true;
                    break;
                } 
            }
            if (breakBool) { break; }
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
