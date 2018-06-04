package nain.himanshu.chatapp;

public class Config {

    public static String BASE_URL = "http://192.168.43.243:8080";
    public static String LoginPrefs = "Login";

    public static String START_CONVERSATION = BASE_URL + "/chat/newConversation";
    public static String SEND_MESSAGE = BASE_URL + "/chat/reply";
    public static String GET_CONVERSATION = BASE_URL + "/chat/oneConversation";

    public static final String CHAT_NOTIF_CHANNEL = "CHATS";
    public static final String GENERAL_NOTIF_CHANNEL = "GENERAL";

}
