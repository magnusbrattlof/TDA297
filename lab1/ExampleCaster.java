
import mcgui.*;
import java.util.ArrayList;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {

    private int[] vectorArray;
    private ArrayList<ExampleMessage> buffer;

    public void init() {
        mcui.debug("The network has " + hosts + " hosts!");
        vectorArray = new int[hosts];
        buffer = new ArrayList<ExampleMessage>();
    }
        
    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {

        vectorArray[id] ++;
        messagetext = " -- vector clock value: " + vectorArray[id] + " -- messagetext: " + messagetext;

        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i,new ExampleMessage(id, messagetext, vectorArray[id]));
            }
        }
        mcui.debug("Sent out: \""+messagetext+"\"");
        mcui.deliver(id, messagetext, "from myself!");
    }
    
    /**
     * Receive a basic message
     * @param message  The message received
     */
    public void basicreceive(int peer,Message message) {
        ExampleMessage eMessage = (ExampleMessage)message;

        if (eMessage.getVectorValue() != vectorArray[eMessage.getSender()] + 1 ) {

            // save message to a buffer (one for each sender), this is done if messages are not deleiverd in correct order
            buffer.add(eMessage); 
        
        } else {

            // this if-statement is not really necessary as it would be okey to just overwite the correct value in the array with the same value
            if (eMessage.getSender() != id) { 
                vectorArray[eMessage.getSender()] = eMessage.getVectorValue();
            } 
            mcui.deliver(peer, eMessage.getText());

            // check if buffer messages can be added 
            checkBuffer(eMessage);

            
        }
    }

    private void checkBuffer(ExampleMessage latestMsg){
        for (ExampleMessage bufferMessage : buffer) {
            if (bufferMessage.getSender() == latestMsg.getSender() && bufferMessage.getVectorValue() == vectorArray[latestMsg.getSender()] + 1) {

                vectorArray[bufferMessage.getSender()] = bufferMessage.getVectorValue();
                System.out.println("Added value from buffer");
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
