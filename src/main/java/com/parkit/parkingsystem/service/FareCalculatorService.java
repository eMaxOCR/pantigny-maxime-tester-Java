package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        long duration = outHour - inHour; //Calculate time between enter and exit
        int msInAnHour = 60*60*1000; //To convert milliseconds to hour.
        int freeTimeInMs = 1800000; //Set 30 minutes in milliseconds

        if(duration <= freeTimeInMs){
        	ticket.setPrice(0);
        	
        }else {
	        switch (ticket.getParkingSpot().getParkingType()){
	            case CAR: {
	                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR / msInAnHour);
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR / msInAnHour);
	                break;
	            }
	            default: throw new IllegalArgumentException("Unkown Parking Type");
	        }
        }
    }
}
