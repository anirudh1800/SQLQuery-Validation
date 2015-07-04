import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    //TODO update keywords
    static final List<String> keywords = Arrays.asList(new String[]{"CREATE",
            "TABLE", "INSERT", "SELECT", "INTO", "FROM", "WHERE", "*", "(", ")",
            "MDSYS.SPATIAL_INDEX", "INDEXTYPE", "PRIMARY KEY",
            "REFERENCES", "NULL", "NOT", "LIKE", "IN", "BETWEEN", "OR", "AND",
            "=", ">", "<", "<=", ">="});

    static final List<String> dataTypes = Arrays.asList(new String[]{
            "INT","STRING", "VARCHAR", "INTEGER", "DATE", "NVARCHAR", "CHAR",
            "VARCHAR2", "SDO_GEOMETRY", "CHARACTER", "BOOLEAN", "BINARY", "SMALLINT",
            "BIGINT", "DECIMAL", "NUMERIC", "FLOAT", "REAL", "TIME", "XML",
            "SDO_ELEM_INFO_ARRAY", "SDO_ORDINATE_ARRAY" };

    public enum State {
        CREATE,
        TABLE,
        NAME,
        KEYWORD,
        ATTRIBUTE_TYPE,
        ENDED
    }

    public static State currentState;
    public static Event currentEvent;

    public enum Event{
        SPACE(" vjhgj"),
        COMMA(','),
        OPEN_BRACE('('),
        CLOSEBRACE(')'),
        END(';')
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputStr = null;
        String[] query;

        while((inputStr = scanner.nextLine()) != null) {
            String input = new String();
            int i = 0;

            while(Character.isLetter(inputStr[i]) || Character.isDigit(inputStr[i]){
                input += inputStr[i++];
            }

            switch(input){
                case "CREATE": currentState = State.CREATE:
                    break;
                case  "TABLE": currentState = State.TABLE:
                    break;
                default : if(input.length() > 0 && dataTypes.contains(input))
                    currentState = State.ATTRIBUTE_TYPE;
                else if(input.length() > 0 && keywords.contains(input))
                    currentState = State.KEYWORD;
                else currentState = State.NAME;
                    break;
            }

            switch(inputStr[i]){
                case " ": currentEvent = Event.SPACE;
                    // TODO set current state based on previous state
                    break;

                case Event.COMMA: currentEvent = Event.COMMA;
                    setCurrentState(State.ATTRIBUTE_NAME);
                    break;
                case Event.OPEN_BRACE: currentEvent = Event.OPEN_BRACE;
                    setCurrentState(State.ATTRIBUTE_NAME);
                    break;
                case Event.CLOSE_BRACE: currentEvent = Event.CLOSE_BRACE;
                    setCurrentState(State.ENDED);
                    break;
                default : System.out.println("Syntax Error at ith positiion");
                    break;
            }
        }
    }



    private static void select_check(String[] query) {

    }

    private static void insert_check(String[] query) {
    }

    private static void create_check(String[] query) {
    }
}