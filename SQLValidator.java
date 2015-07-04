import java.util.*;

class ParseException extends Exception {
    public ParseException(String s) {
        super(s);
    }
}

public class Main {

    static final List<String> keywords = Arrays.asList(new String[]{"CREATE",
            "TABLE", "INSERT", "SELECT", "INTO", "FROM", "WHERE", "*", "(", ")",
            "MDSYS.SPATIAL_INDEX", "INDEXTYPE", "PRIMARY", "KEY",
            "REFERENCES", "NULL", "NOT", "LIKE", "IN", "BETWEEN", "OR", "AND",
            "=", ">", "<", "<=", ">="});

    static final List<String> dataTypes = Arrays.asList(new String[]{
            "INT", "STRING", "VARCHAR", "INTEGER", "NUMBER", "DATE", "NVARCHAR", "CHAR",
            "VARCHAR2", "SDO_GEOMETRY", "CHARACTER", "BOOLEAN", "BINARY", "SMALLINT",
            "BIGINT", "DECIMAL", "NUMERIC", "FLOAT", "REAL", "TIME", "XML",
            "SDO_ELEM_INFO_ARRAY", "SDO_ORDINATE_ARRAY"});

    static final HashSet<String> keywordSet = new HashSet<>(keywords);
    static final HashSet<String> dataTypeSet = new HashSet<>(dataTypes);
    static HashMap<String, String> variableMaps = new HashMap<>();
    static int pos = 0;

    public enum State {

        INITIAL {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (word.equalsIgnoreCase("CREATE")) {
                    return State.CREATE;
                } else if (word.isEmpty()) {
                    return State.INITIAL;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        CREATE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (word.equalsIgnoreCase("TABLE") && event == ' ') {
                    return State.TABLE;
                } else if (word.isEmpty()) {
                    return State.CREATE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        TABLE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (isName(word)) {
                    if (event == ' ') {
                        if (variableMaps.containsValue(word))
                            throw new ParseException("Table already exists:" + (pos + 1));
                        else variableMaps.put("TABLE_NAME", word);
                        return State.NAME;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.isEmpty()) {
                    return State.TABLE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        NAME {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == '(' && isName(word)) {
                    variableMaps.put("VARIABLE_NAME", word);
                    return State.NAME;
                } else if (event == ' ' && dataTypeSet.contains(word)) {
                    return State.ATTRIBUTE_TYPE;
                } else if (word.isEmpty())
                    return State.NAME;
                else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        KEYWORD {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && keywordSet.contains(word)) {
                    return State.KEYWORD;
                } else if (event == ',' && isName(word)) {
                    return State.NAME;
                } else if (event == ')') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.KEYWORD;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        ATTRIBUTE_TYPE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && keywordSet.contains(word)) {
                    return State.KEYWORD;
                } else if (event == ',' && isName(word)) {
                    return State.NAME;
                } else if (event == ')') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.ATTRIBUTE_TYPE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        END {
            @Override
            public State nextState(String word, Character event) {
                return State.END;
            }
        };

        public abstract State nextState(String word, Character event) throws ParseException;
    }

    public static boolean isName(String word) throws ParseException {
        if (word.length() > 0 && !keywordSet.contains(word) && !dataTypeSet.contains(word)) {
            if (variableMaps.containsValue(word))
                throw new ParseException("Variable " + word + " already used:" + (pos + 1));
            else
                return true;
        } else return false;
    }

    public static State currentState = State.INITIAL;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String word = new String();
        Character event = null;

        String input = scanner.nextLine();
        pos = 0;

        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            word += input.charAt(pos++);
        }

        try {
            currentState = currentState.nextState(word, event);

            while (currentState != State.END) {
                String next_word = new String();

                if (!word.isEmpty())
                    event = input.charAt(pos++);
                else if (word.isEmpty() && event == ' ') {
                    event = input.charAt(pos++);
                } else pos++;

                while (pos < input.length() && (Character.isLetter(input.charAt(pos)) || (Character.isDigit(input.charAt(pos)) && next_word.length() > 1))) {
                    next_word += input.charAt(pos++);
                }
                word = next_word;
                currentState = currentState.nextState(word, event);
            }

            if (currentState == State.END) {
                System.out.println("Successfully parsed query:" + (pos + 1));
            }
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void select_check(String[] query) {
    }

    private static void insert_check(String[] query) {
    }

    private static void create_check(String[] query) {
    }
}