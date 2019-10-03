package it.at.restfs;

import org.apache.commons.lang3.StringUtils;

import lombok.SneakyThrows;

class ExpectationsBuilder {

	public void expected(String expected, String result) {
        if(! StringUtils.equals(expected, result)){
            
            System.err.println("expected: " + expected);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }
    }    

	public void match(String match, String result) {
        if(! result.matches(match)){
            
            System.err.println("match: " + match);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }
    }
    
    public void matchEverywhere(String match, String result) {        
        if(! StringUtils.contains(result, match)){
            
            System.err.println("match: " + match);
            System.err.println("result: " + result);
            
            throw new RuntimeException("Not the same !!?");
        }        
    }
    
    @SneakyThrows
    public void wait(int seconds) {
        Thread.sleep(seconds * 1000);
    }
    
}
