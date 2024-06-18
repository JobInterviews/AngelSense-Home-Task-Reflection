package com.angelsense;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utility {
    private static final Logger logger = Logger.getLogger(Utility.class.getName());
    private static final String LINE = "----------------------";
    private static final String INDENT_STRING = "\t";
    private static final String EQUALS = " = ";
    private static final String CLASS_TITLE = "Object of Class ";
    private static final String NEW_LINE = "\n";
    private static final String QUOTATION_MARK = "\"";

    private StringBuilder result;

    public StringBuilder analyze(Struct obj) throws IllegalArgumentException, IllegalAccessException {
        result = new StringBuilder();
        analyze(obj, 0);
        return result;
    }

    private void analyze(Object obj, int level) throws IllegalArgumentException, IllegalAccessException {
        if(obj == null)
            return;

        Class<?> objClass = obj.getClass();
        printClassTitle(obj, level);
        Field[] fields = objClass.getDeclaredFields();

        for(Field field : fields) {
            String fieldName = field.getName();
            Object fieldValue = field.get(obj);

            if(isString(field) || isPrimitive(field)){
                printField(fieldName, fieldValue+"", level);
            }else if(isCollection(field)) {
                analyzeCollection(fieldName, fieldValue, level);
            }else if(isArray(field)) {
                analyzeArray(fieldName, fieldValue, level);
            }else { //struct
                printField(fieldName, level);
                analyze(fieldValue, ++level);
            }
        }
    }

    private void printClassTitle(Object obj, int level) {
        result.append(NEW_LINE).append(leftPadding(CLASS_TITLE + QUOTATION_MARK + getName(obj) + QUOTATION_MARK, level)).append(NEW_LINE);
        result.append(leftPadding(LINE, level)).append(NEW_LINE);
        //System.out.println("\n" + leftPadding(CLASS_TITLE + " \"" + getName(obj) + "\"", level));
        //System.out.println(leftPadding(LINE, level));
    }

    private boolean isString(Field field){
        return String.class.isAssignableFrom(field.getType());
    }

    private boolean isPrimitive(Field field){
        return field.getType().isPrimitive();
    }

    private boolean isCollection(Field field){
        return Collection.class.isAssignableFrom(field.getType());
    }

    private boolean isArray(Field field) {
        return field.getType().isArray();
    }

    private void analyzeCollection(String fieldName, Object fieldValue, int level) throws IllegalAccessException {
        if(fieldValue == null)
            printField(fieldName, null, level);
        else {
            printField(fieldName, level);
            analyzeCollection(fieldValue, level);
        }
    }

    private void analyzeCollection(Object collection, int level) throws IllegalAccessException {
        level++;

        for (Object o : (Collection<?>) collection)
            if(o != null)
                analyze(o, level);
    }

    private void analyzeArray(String fieldName, Object fieldValue, int level) throws IllegalAccessException {
        if(fieldValue == null)
            printField(fieldName, null, level);
        else if(isPrimitiveArray(fieldValue))
            printField(fieldName, getArrayValue(fieldValue), level);
        else {
            printField(fieldName, level);
            analyzeStructsArray(fieldValue, level);
        }
    }

    private void printField(String fieldName, int level) {
        result.append(leftPadding(fieldName + EQUALS, level));
        //System.out.print(leftPadding(fieldName + EQUALS, level));
    }
    private void printField(String fieldName, String fieldValue, int level) {
        result.append(leftPadding(fieldName + EQUALS + fieldValue, level)).append(NEW_LINE);
        //System.out.println(leftPadding(fieldName + EQUALS + fieldValue, level));
    }

    private String leftPadding(String input, int length) {
        return INDENT_STRING.repeat(length) + input;
    }

    private String getArrayValue(Object array) {
        return Arrays.toString(toArray(array));
    }

    private Object[] toArray(Object array) {
        int len = Array.getLength(array);
        Object[] res = new Object[len];
        for (int i = 0; i < len; i++)
            res[i] = Array.get(array, i);

        return res;
    }

    private boolean isPrimitiveArray(Object array) {
        return array.getClass().getComponentType().isPrimitive();
    }

    @SuppressWarnings("unchecked")
    private void analyzeStructsArray(Object array, int level) throws IllegalAccessException {
        level++;

        for (Object o : (Object[]) array)
            if(o != null)
                analyze(o, level);
    }

//    private void printRepeatedClass(Object obj, int level) {
//        System.out.println("\n" + leftPadding("Object of Class " + getName(obj), level) + " was printed before");
//        System.out.println(leftPadding(LINE, level));
//    }

    private String getName(Object obj) {
        return obj.getClass().getSimpleName();
    }



    public static void main(String[] args) {
        Name n = new Name();
        n.firstName = "John";
        n.lastName = "Doe";
        n.salary = 0;

        Name n1 = new Name();
        n1.firstName = "John1";
        n1.lastName = "Doe1";
        n1.salary = 1;

        Name n2 = new Name();
        n2.firstName = "John2";
        n2.lastName = "Doe2";
        n2.salary = 2;

        Name n3 = new Name();
        n3.firstName = "John3";
        n3.lastName = "Doe3";

        Name n4 = new Name();
        n4.firstName = "John4";
        n4.lastName = "Doe4";

        n.children = List.of(n1, n2);
        n.arrayOfStructs = new Object[]{n1, n2};
        n1.arrayOfStructs = new Object[]{n3};
        n3.children = List.of(n4);


        n.arrayOfPrimitives = new int[]{1,2};

        Person p = new Person();
        p.age = 55;
        p.name = n;


        Utility utility  = new Utility();

        try {
            System.out.println(utility.analyze(p));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private static class Name implements Struct{
        public String firstName;
        public String lastName;
        public List<?> children;
        public Object[] arrayOfStructs;
        public int[] arrayOfPrimitives;
        public long salary;
    }
    
    private static class Person implements Struct{
        public int age;
        public Name name;
    }
}
