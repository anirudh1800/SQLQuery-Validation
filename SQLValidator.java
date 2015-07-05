import java.util.*;

/*
    created by anirudhgali

 */

public class SQLValidator {

    static final List<String> keywords = Arrays.asList(new String[]{"CREATE",
            "TABLE", "INSERT", "SELECT", "INTO", "FROM", "WHERE",
            "MDSYS.SPATIAL_INDEX", "INDEXTYPE", "PRIMARY", "KEY",
            "REFERENCES", "FOREIGN", "NULL","UNION", "UPDATE", "DROP", "DELETE", "ALTER"});

    static final List<String> dataTypes = Arrays.asList(new String[]{"CHAR", "VARCHAR", "VARCHAR2", "TINYTEXT", "TEXT", "BLOB", "MEDIUMTEXT",
            "MEDIUMBLOB", "LONGTEXT", "LONGBLOB", "ENUM", "SET", "NUMBER",
            "TINYINT", "SMALLINT", "MEDIUMINT", "INT", "BIGINT", "FLOAT", "DOUBLE",
            "DECIMAL", "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR", "SDO_ELEM_INFO_ARRAY", "SDO_ORDINATE_ARRAY", "SDO_GEOMETRY"});

    static final HashSet<String> keywordSet = new HashSet<>(keywords);
    static final HashSet<String> dataTypeSet = new HashSet<>(dataTypes);
    static HashMap<String, String> variableMaps = new HashMap<>();
    static HashMap<String, String> variableTypes = new HashMap<>(); // TODO

    public static State initialState = State.INITIAL;
    public static String input;
    static int pos = 0;
    static String left_operand = null;
    static String right_operand = null;
    static String operator = null;

    public enum State {

        INITIAL {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                switch (word.toUpperCase()) {

                    case "CREATE":
                        return State.CREATE;

                    case "INSERT":
                        return State.INSERT;

                    case "SELECT":
                        return State.SELECT;

                    case "":
                        return State.INITIAL;

                    default:
                        throw new ParseException("Syntax error in query:" + (pos + 1));
                }
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

        INSERT {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (word.equalsIgnoreCase("INTO") && event == ' ') {
                    return State.INTO;
                } else if (word.isEmpty()) {
                    return State.INSERT;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        INTO {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (isName(word)) {
                    if (event == ' ') {
                        return State.NAME;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.isEmpty()) {
                    return State.INTO;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        SELECT {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && isName(word)) {
                    return State.NAME;
                } else if (word.equalsIgnoreCase("*")) {
                    return State.STAR;
                } else if (word.isEmpty()) {
                    return State.SELECT;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        FROM {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && isName(word)) {
                    return State.TABLE_NAME;
                } else if (word.isEmpty()) {
                    return State.FROM;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        STAR {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if(event == ' ' && word.equalsIgnoreCase("FROM")) {
                    return State.FROM;
                } else if (word.isEmpty()) {
                    return State.STAR;
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
                } else if (event == ' ' && word.equalsIgnoreCase("FROM")) {
                    return State.FROM;
                } else if (event == ',' && isName(word) && initialState == SELECT) {
                    return State.NAME;
                } else if (event == ' ' && isOperator(word)) {
                    if (left_operand != null) {
                        operator = word;
                        return State.COND;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.equalsIgnoreCase("VALUES")) {
                    return State.VALUES;
                } else if (event == ';') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.NAME;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        TABLE_NAME {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && word.equalsIgnoreCase("WHERE")) {
                    left_operand = null;
                    right_operand = null;
                    operator = null;
                    return State.WHERE;
                } else if (event == ';') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.TABLE_NAME;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        WHERE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && isName(word)) {
                    left_operand = word;
                    return State.NAME;
                } else if (word.isEmpty()) {
                    return State.WHERE;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        COND {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ' ' && isName(word)) {
                    if (left_operand != null && operator != null) {
                        right_operand = word;
                        return State.NAME;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                }
                if (event == ' ' && isValue(word)) {
                    if (left_operand != null && operator != null) {
                        right_operand = word;
                        return State.VALUE;
                    } else throw new ParseException("Syntax error in query:" + (pos + 1));
                } else if (word.isEmpty()) {
                    return State.TABLE_NAME;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
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

        VALUES {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == '(' && isValue(word)) {
                    return State.VALUE;
                } else if (word.isEmpty()) {
                    return State.VALUES;
                } else throw new ParseException("Syntax error in query:" + (pos + 1));
            }
        },

        VALUE {
            @Override
            public State nextState(String word, Character event) throws ParseException {
                if (event == ',' && isValue(word)) {
                    return State.VALUE;
                } else if (event == ')') {
                    return State.END;
                } else if (event == ';') {
                    return State.END;
                } else if (word.isEmpty()) {
                    return State.VALUE;
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String word = new String();
        Character event = null;
        State currentState = State.INITIAL;

        System.out.println("Please type the query:");
        input = scanner.nextLine();
        pos = 0;

        System.out.print("Validating.");
        try {
            Thread.sleep(300);
            System.out.print("..");
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("...");

        while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
            word += input.charAt(pos++);
        }

        try {
            currentState = currentState.nextState(word, event);
            initialState = currentState;

            while (currentState != State.END && pos < input.length()) {
                String next_word = new String();

                if (!word.isEmpty())
                    event = input.charAt(pos++);
                else if (word.isEmpty() && event == ' ') {
                    event = input.charAt(pos++);
                } else pos++;

                switch (currentState) {

                    case VALUES:
                    case VALUE:
                        boolean flag = false;
                        while (pos < input.length() && !isDelim(input.charAt(pos))) {
                                    next_word += input.charAt(pos++);
                        }
                        break;

                    default:
                        while (pos < input.length() && !isDelim(input.charAt(pos))){
                            if (Character.isLetter(input.charAt(pos)))
                                next_word += input.charAt(pos++);
                            else if((input.charAt(pos) == '_' || Character.isDigit(input.charAt(pos))) && next_word.length() > 1)
                                next_word += input.charAt(pos++);
                            else if(isOperator(input.charAt(pos)))
                                next_word += input.charAt(pos++);
                            else if(input.charAt(pos) == '\'' || input.charAt(pos) == '*'){
                                next_word += input.charAt(pos++);
                            } else throw new ParseException("Syntax error in query:" + (pos + 1));
                         }
                        break;
                }
                word = next_word;
                currentState = currentState.nextState(word, event);
            }

            if (currentState == State.END) {
                System.out.println("Successfully parsed query:" + (pos + 1));
            } else throw new ParseException("Syntax error in query:" + (pos + 1));
        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static boolean isName(String word) throws ParseException {
        if (word.length() > 0 && !keywordSet.contains(word.toUpperCase()) && !dataTypeSet.contains(word.toUpperCase()) && !word.startsWith("'")) {
            if (initialState == State.CREATE && variableMaps.containsValue(word))
                throw new ParseException("Variable " + word + " already used:" + (pos + 1));
            else
                return true;
        } else return false;
    }

    public static boolean isValue(String word) throws ParseException {
        if (word.length() > 0) {
            if (isInteger(word) || isText(word) || isFloat(word))
                return true;
            else if (isSdogeometry(word)) {
                int tmp = pos;
                if (input.charAt(pos) == '(') {
                    while (input.charAt(pos) != ')' && pos < input.length()) {
                        pos++;
                    }
                    if (pos == input.length())
                        throw new ParseException("SDO_GEOMETRY type error:" + (tmp + 1));
                    pos++;
                    return true;
                } else throw new ParseException("SDO_GEOMETRY type error:" + (tmp + 1));

            } else {
                return false;
            }
        }
        return false;
    }

    private static boolean isText(String word) {
        if (word.startsWith("'") && word.endsWith("'"))
            return true;
        else
            return false;
    }

    public static boolean isSdogeometry(String word) {
        if (word.startsWith("SDO_GEOMETRY")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOperator(String word) {
        return word.matches("=?|!=?|>=?|<=?|>?|<?");
    }

    public static boolean isOperator(char ch) {
        switch(ch){

            case '=':
            case '!':
            case '>':
            case '<':
                return true;

            default:
                return false;
        }
    }

    public static boolean isFloat(String word) {
        boolean isValidFloat = false;
        try {
            Float.parseFloat(word);
            isValidFloat = true;
        } catch (NumberFormatException ex) {
        }
        return isValidFloat;
    }

    public static boolean isInteger(String word) {
        boolean isValidInteger = false;
        try {
            Integer.parseInt(word);
            isValidInteger = true;
        } catch (NumberFormatException ex) {
        }
        return isValidInteger;
    }


    public static boolean isDelim(char ch) {
        switch (ch) {

            case ' ':
            case '(':
            case ')':
            case ',':
            case ';':
                return true;

            default:
                return false;
        }
    }
}

class ParseException extends Exception {
    public ParseException(String s) {
        super(s);
    }
}
