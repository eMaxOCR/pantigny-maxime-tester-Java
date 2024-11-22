package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	public void calculateFare(Ticket ticket) {
		calculateFare(ticket,false);
	}

    public void calculateFare(Ticket ticket, Boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long duration = outHour - inHour; //Calculate time between enter and exit
        int msInAnHour = 60*60*1000; //To convert milliseconds to hour.
        int freeTime = 30*60*1000; //Set 30 minutes
        double discountMultiplicator = 1; //Set discountMultiplicator to 1 by default
        double discountValue = 0.95; //Set 5% discount value.
        
        //If ticket has discount parameter true, then set discount value to 5%
        if(discount){
        	discountMultiplicator = discountValue;
        }
        
        //If duration is less or equal than 30 minutes, ticket is free
        if(duration <= freeTime){
        	ticket.setPrice(0);
     
        }else {
	        switch (ticket.getParkingSpot().getParkingType()){
	            case CAR: {
	                ticket.setPrice((duration * (Fare.CAR_RATE_PER_HOUR * discountMultiplicator)) / msInAnHour);
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice((duration * (Fare.BIKE_RATE_PER_HOUR * discountMultiplicator)) / msInAnHour);
	                break;
	            }
	            default: throw new IllegalArgumentException("Unkown Parking Type");
	        }
        }
    }
}
