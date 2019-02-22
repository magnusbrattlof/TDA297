
import mcgui.*;

/**
 * Message implementation for ExampleCaster.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleMessage extends Message {
        
    private String status;
    private String text;
    private int sequence;
    private int localSeq;
    private int localID;
    private int globalSeq;
    private int globalID;
    private int proposeSeq;
    private int proposeID;

    //messagetext = "req" + ":" + messagetext + ":" + id + ":" + vectorArray[id]+1;
    public ExampleMessage(String status, String text, int sender, int sequence) {
        this.status = status;
        this.text = text;
        super(sender);
        this.sequence = sequence;
    }

    // messagetext = "seq" + ":" + id + ":" + vectorArray[id]+1 ":" + global[0] + ":" + global[1];
    public ExampleMessage(String status, int localSeq, int localID, int globalSeq, int globalID) {
        this.status = status;
        this.localSeq = localSeq;
        this.localID = localID;
        this.globalSeq = globalSeq;
        this.globalID = globalID;

    }

    //messagetext = "pro" + ":" + proposeSeq + ":" + proposeID;
    public ExampleMessage(String status, int proposeSeq, int proposeID) {
        this.status = status;
        this.proposeSeq = proposeSeq;
        this.proposeID = proposeID;
    }

    /**
     * Returns the text of the message only. The toString method can
     * be implemented to show additional things useful for debugging
     * purposes.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Returns vectorVlaue of message, this info together with id of
     * sender complets all info needed for vector clock 
     */
    public int getSequence() {
        return sequence;
    }

    public String getStatus(){
        return status;
    }
    public int getLocalSeq(){
        return localSeq;
    }
    public int getLocalID(){
        return localID;
    }
    public int getGlobalSeq(){
        return globalSeq;
    }
    public int getGlobalID(){
        return globalID;
    }
    public int getProposeSeq(){
        return proposeSeq;
    }
    public int getProposeID(){
        return proposeID;
    }

    public static final long serialVersionUID = 0;
}
