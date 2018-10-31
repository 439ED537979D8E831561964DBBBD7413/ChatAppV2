package nain.himanshu.chatapp;

public class Config {

    public static String BASE_URL = "http://10.60.31.111:8080";
    //public static String BASE_URL = "https://sp-chat-app.herokuapp.com";
    public static String LoginPrefs = "Login";

    public static String USER_BASE_URL = BASE_URL + "/user/";

    public static String START_CONVERSATION = BASE_URL + "/chat/newConversation";
    public static String SEND_MESSAGE = BASE_URL + "/chat/reply";
    public static String GET_CONVERSATION = BASE_URL + "/chat/oneConversation";
    public static String HAS_CONVERSATION = BASE_URL + "/chat/hasConversation";

    public static final String CHAT_NOTIF_CHANNEL = "CHATS";
    public static final String GENERAL_NOTIF_CHANNEL = "GENERAL";

}
