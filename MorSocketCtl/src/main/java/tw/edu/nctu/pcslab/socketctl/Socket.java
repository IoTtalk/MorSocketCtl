package tw.edu.nctu.pcslab.socketctl;

/**
 * Created by kuan on 2017/8/31.
 */

public class Socket {
    public String alias;
    public Integer index;
    public Boolean state; //true:on, false:off
    Socket(String alias, Integer index, Boolean state){
        this.alias = alias;
        this.index = index;
        this.state = state;
    }
}
