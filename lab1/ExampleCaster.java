
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
    private ArrayList<ExampleMessage> finalBuffer;

    public void init() {
        mcui.debug("The network has " + hosts + " hosts!");
        proposalBuffer = new ArrayList<ArrayList<ExampleMessage>>();
        for (int i = 0; i < hosts; i ++) {
            proposalBuffer.add(new ArrayList<ExampleMessage>());
        }
        finalBuffer = new ArrayList<ExampleMessage>();
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {

        // phase 1 -- requesting clockvalues, sening the msg and a unique identifier (id, potential local clock vlaue) NOTE: logic of local clock value might be worng
        ExampleMessage eMessage = new ExampleMessage("req", messagetext, id, globalSequence, localSequence);
        send(eMessage);
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

        // PHASE 1 -- recives request for proposal and message, sending propsal
        if (eMessage.getStatus().equals("req")) {
            phase1(eMessage);
        
        // PHASE 2 -- recives proposal, broadcasting the new message with new correct clock-value
        } else if (eMessage.getStatus().equals("pro")) {
            phase2(eMessage);

            
        // PHASE 3 -- recives global clovk value/sequence for a message, i.e. actually deliver msg
        } else if (eMessage.getStatus().equals("fin")) {
            phase3(eMessage);

        // else error
        } else {
            mcui.debug("Error - basicrecive() failed to find right phase");   
        }
    }

    // PHASE 1 -- recives request for proposal and message; sending propsal and saving message. message is maped ot sender and sender local sequence
    public void phase1(ExampleMessage eMessage) {
        p = Math.max(a, p) + 1;
        ExampleMessage answer = new ExampleMessage("pro", eMessage.getText(), id, p, eMessage.getLocalSequence());
        bcom.basicsend(eMessage.getSender(), answer); // TODO: r-boradcast this?
    }

    // PHASE 2 -- recives proposal, broadcasting the new message with new correct clock-value
    public void phase2(ExampleMessage eMessage) {

        int index = eMessage.getLocalSequence() % hosts;

        if (!proposalBuffer.get(index).contains(eMessage)) {
            proposalBuffer.get(index).add(eMessage);
        }

        if (proposalBuffer.get(index).size() == hosts - 1) {
            int propMax = 0; 
            int propID = 0;
            for (ExampleMessage tmp : proposalBuffer.get(index)) {
                if (tmp.getSequence() > propMax) { 
                    propMax = tmp.getSequence();        // tmp.getSender() proposese tmp.getSequence()
                    propID = tmp.getSender();
                }
            }
            globalSequence = propMax;
            globalID = propID;
            //System.out.println("I AM " + id + " ----  my txt should be same as you wronte in my client: " +eMessage.getText());
            ExampleMessage answer = new ExampleMessage("fin", eMessage.getText(), globalID, globalSequence, localSequence);    // TODO: test if "id" can be changed to "propID"
            
            
            deliver2self(answer);
            
            
            send(answer); // TODO: r-boradcast this? 
            proposalBuffer.get(index).clear();
        } 
    }

    // PHASE 3 -- recives final clovk value/sequence for a message, i.e. actually deliver msg
    public void phase3(ExampleMessage eMessage) {
        //System.out.println("I AM " + id + " ----  I just recived a final deliver on text:  " + eMessage.getText() + " ---- from: " + eMessage.getSender() + " ---- proposed id: " + eMessage.getSequence());
        a = Math.max(eMessage.getSequence(), a);
        deliver2self(eMessage);
    }

    public void deliver2self(ExampleMessage eMessage) {
        if (!finalBuffer.contains(eMessage)){   // TODO: not optimal solution to duplicated enteies problem...
            finalBuffer.add(eMessage);
            Collections.sort(finalBuffer); 
        } 

        //String str1 = ("NEW FOR-LOOP BELOW --- " + id);
        //mcui.debug(str1);
        for (int i = 0; i < finalBuffer.size(); i ++) {
            //System.out.println("I AM " + id + " --- my buffer is -> text: " + finalBuffer.get(i).getText() + " global sequence: " + finalBuffer.get(i).getSequence() + " - id:  " + finalBuffer.get(i).getSender() );
            //String str2 = ("I AM " + id + " --- my buffer is -> text: " + finalBuffer.get(i).getText() + " global sequence: " + finalBuffer.get(i).getSequence() + " - id:  " + finalBuffer.get(i).getSender() );
            //mcui.debug(str2);

            if (i > hosts && finalBuffer.get(i-1).getSequence() < eMessage.getSequence()) {
                mcui.deliver(finalBuffer.get(i-1).getSender(), finalBuffer.get(i-1).getText());   //TODO: not sure if this "getSender()" is who we think it is... the plot thickens...
                finalBuffer.remove(i-1);
                Collections.sort(finalBuffer); 
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
