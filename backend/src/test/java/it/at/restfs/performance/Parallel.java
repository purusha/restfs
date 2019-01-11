package it.at.restfs.performance;

import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.reflections.Reflections;
import it.at.restfs.BaseTest;

public class Parallel {
    
    private final Reflections reflections = new Reflections("it.at.restfs.integration");
    
    @Test
    public void test() {

        Set<Class<? extends BaseTest>> allClasses = reflections.getSubTypesOf(BaseTest.class);
        System.out.println(allClasses);
        
        final Class<?>[] array = new Class<?>[allClasses.size()]; 
        
        //run
        final Result realRun = realRun(allClasses.toArray(array));
        
        realRun.getFailures().forEach(f -> System.out.println(f));
        
        Assert.assertEquals(0, realRun.getFailureCount());        
    }
    
//    public static <T> T[] createArray(Class<T> type, int size){
//        return (T[])Array.newInstance(type, size);
//    }    
    
    private Result realRun(Class<?>[] cls) {
        
        // Parallel among classes
        // return JUnitCore.runClasses(ParallelComputer.classes(), cls);
        
        // Parallel among methods in a class
        // return JUnitCore.runClasses(ParallelComputer.methods(), cls);
        
        // Parallel all methods in all classes
        return JUnitCore.runClasses(new ParallelComputer(true, true), cls);
        
    }
    
}
