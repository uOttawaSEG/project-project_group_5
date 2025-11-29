package ca.uottawa.seg.otams;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
public class UnitTests {

    Student student;
    @Before
    public void setUp(){
        student=new Student("John","Smith","john.smith@gmail.com","password","1234567890","Software Engineering");
    }

    @Test
    public void testGetFirstName(){
        String firstName="John"; //initalize a first name
        String actualFirstName=student.getFirstName(); //get the first name
        assertEquals(firstName,actualFirstName); //check to see if they are the same first name

    }

    @Test
    public void testSetFirstName(){
        String firstName="William";//initialize first name
        student.setFirstName(firstName); //set the first name to be William
        String actualFirstName=student.getFirstName(); //get the first name
        assertEquals(firstName,actualFirstName); //check to see if they are the same first name

    }

    @Test
    public void testGetLastName(){
        String lastName="Smith"; //initalize a last name
        String actualLastName=student.getLastName(); //get the last name
        assertEquals(lastName,actualLastName); //check to see if they are the same last name

    }

    @Test
    public void testSetLastName(){
        String lastName="Johnson";//initialize last name
        student.setLastName(lastName); //set the last name to be William
        String actualLastName=student.getLastName(); //get the last name
        assertEquals(lastName,actualLastName); //check to see if they are the same last name

    }




}
