package cn.wildfire.imshat.kit.conversation.message.viewholder;

import cn.wildfirechat.message.Message;

public class IntentHelper {

    private static Message message;
    private static IntentHelper instance;

    public static IntentHelper getInstance(){

        if(instance==null){
            return  new IntentHelper();
        }else{
            return instance;
        }

    }

    public Message getForwordMsg(){
       return this.message;
    }

    public void setForwordMsg(Message message){
        this.message=message;
    }

}
