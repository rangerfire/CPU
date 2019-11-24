package wang;

import java.util.ArrayList;
import java.util.Scanner;

public class BusSim {
	
	public static double clock;
	public static int busnumber;
	public static int stopnumber;
	public static int drivingtime;
	public static double boardingtime;
	public static int stoptime;
	public static int meanarrivalrate;
	
	public static busstop[] bs;
		
	public static ArrayList<event> event_queue = new ArrayList<event>();
	
	public static int[] wqueue_length = new int[15];
	public static int[] stop_count = new int[15];
	public static int[] max = new int[15];
	public static int[] min = new int[15];
	
	public static ArrayList<position> bus_position = new ArrayList<position>();
	
	public static void Initiallzation(int busnumber1, int stopnumber2,int drivingtime3,
			double boardingtime4,int meanarrivalrate5,int stoptime6)
	{
		clock = 0;
		busnumber = busnumber1;
		stopnumber = stopnumber2;
		drivingtime = drivingtime3;				//minute
		boardingtime = boardingtime4;			//second
		meanarrivalrate = meanarrivalrate5;		// person/minute
		stoptime = stoptime6;					//minute
		
		bs = new busstop[stopnumber];
		for(int i=0;i<stopnumber;i++)
		{
			bs[i] = new busstop(0, 5);
		}
		
		//uniformly
		//generate person event for each stop
		for(int i=0;i<stopnumber;i++)
			generateEvent("person", clock, i, 0);

		for(int j=0;j<busnumber;j++)
		{
			generateEvent("arrival", clock, 3*j, j);
			bus_position.add(new position(j,j*3,clock,true));
		}
	}

	//get next event
	public static event getnextEvent()
	{
		//find the first come
		event e;
		double mini = event_queue.get(0).time;
		int index = 0;
		for(int i=1;i<event_queue.size();i++)
		{
			if(event_queue.get(i).time < mini)
			{	
				mini = event_queue.get(i).time;
				index = i;
			}
		}
		e = event_queue.get(index);
		event_queue.remove(index);
		return e;
	}
	
	//generate random
	
	//generate event
	public static void generateEvent(String type, double time, int stopnumber, int busnumber)
	{
		event e = new event();
		e.type = type;
		e.time = time;
		e.stop_number = stopnumber;
		e.bus_number = busnumber;
		event_queue.add(e);
	}
	
	public static void getBusPosition(int bnumber, double time)
	{
		int pre = 0;
		int pos = bus_position.size()-1;
		
		//find pre pos
		for(int i=0;i<bus_position.size();i++)
		{
			if(bus_position.get(i).bus_number == bnumber)
			{
				double t1 = time - bus_position.get(i).time;
				double t2 = time - bus_position.get(pre).time;
				if( t1>=0 && t1<=t2 )
					pre = i;
				double t3 = bus_position.get(pos).time - time;
				double t4 = bus_position.get(i).time - time;
				if(t4>=0 && t4<=t3)
					pos = i;
			}
		}
		
		if(bus_position.get(pre).time == time)
			System.out.println("This bus is at stop_" + bus_position.get(pre).stop_number);
		else
			if(bus_position.get(pos).time == time)
				System.out.println("This bus is at stop_" + bus_position.get(pos).stop_number);
			else
				if(bus_position.get(pre).atstop && bus_position.get(pos).atstop)
					System.out.println("This bus is at stop_" + bus_position.get(pre).stop_number);
				else
					if(bus_position.get(pre).atstop && !bus_position.get(pos).atstop)
						System.out.println("This bus is at stop_" + bus_position.get(pre).stop_number);
					else
						if(!bus_position.get(pre).atstop && bus_position.get(pos).atstop)
							System.out.println("This bus is on the way to stop_" + bus_position.get(pos).stop_number);
	}
			
	
	
	//main
	public static void main(String[] args) {
		// busnumber = 5
		// stopnumber = 15
		// drivingtime = 5 min
		// boardingtime = 2 sec = 2/60min
		// stoptime = 8*60min
		// meanarrivalrate = 5 p/min
		Initiallzation(5,15,5,(double)(2.0/60),5,480);

		double count = 60;
		
		do
		{
			event nextevent = getnextEvent();
			
			double pre = clock; 
			
			clock = nextevent.time;
			
			double pos = clock;
			if( pre<count && pos>count)
			{
				System.out.println(count/60 + "hour:");
				for(int i=0;i<stopnumber;i++)
				{
//					System.out.println(clock);
					int wl = wqueue_length[i]/stop_count[i];
					System.out.println("Stop_" + i + "'s average waiting queue length:" + wl + " ,max=" + max[i] + " ,min = " + min[i]);
					wqueue_length[i] = 0;
					stop_count[i] = 0;
					max[i] = 0;
					min[i] = bs[i].waitqueue;
				}
				count = count + 60;
			}
			
			switch(nextevent.type)
			{
				case "person" :
				{
					bs[nextevent.stop_number].waitqueue++;
					
					if(max[nextevent.stop_number] <= bs[nextevent.stop_number].waitqueue)
						max[nextevent.stop_number] = bs[nextevent.stop_number].waitqueue;
					
					wqueue_length[nextevent.stop_number] = wqueue_length[nextevent.stop_number] 
																	+ bs[nextevent.stop_number].waitqueue;
					stop_count[nextevent.stop_number]++;
					
					double temp = ( (1.0/meanarrivalrate) * (double)( -Math.log(Math.random()) ) );
					double nexttime = clock + temp;		//random					
					generateEvent("person", nexttime, nextevent.stop_number, nextevent.bus_number);			//bus_number no use
					//System.out.println("Time:" + clock + ", " + "one person arrive at stop_" + nextevent.stop_number);
					break;	
				}
				case "arrival":
				{
					
					int snumber = nextevent.stop_number;
					int bnumber = nextevent.bus_number;
					bs[snumber].bus[bnumber] = 1;
					
					bus_position.add(new position(bnumber, snumber, clock, true));

					int flag = 0;
					int bcount = 0;
					for(int i=0;i<busnumber;i++)
					{
						if(bs[snumber].bus[i] !=0)
						{
							bcount++;
							if(i != bnumber)	
								flag = 1;
						}
					}

					if(flag == 0)
					{
//						System.out.println("Time:" + clock + ", bus_" + bnumber + " arrive at stop_" + snumber
//								+ ", stop have "+ bcount + " bus");
						if(bs[snumber].waitqueue == 0)
						{

							bs[snumber].bus[bnumber] = 0;
							generateEvent("arrival", clock+drivingtime, (snumber+1)%stopnumber, bnumber);
						}
						else
						{

							generateEvent("boarder", clock, snumber, bnumber);
						}
					}
					else
					{

						generateEvent("arrival", clock+boardingtime, snumber, bnumber);
//						System.out.println("Time:" + clock + ", bus_" + bnumber + " is waiting at stop_" + snumber
//								+ ", stop have "+ bcount + " bus");
					}
					
					break;
				}
				case "boarder":
				{
					
					int snumber = nextevent.stop_number;
					int bnumber = nextevent.bus_number;
//					bs[snumber].waitqueue--;

					if(bs[snumber].waitqueue == 0)
					{

						bs[snumber].bus[bnumber] = 0;
						generateEvent("arrival", clock+drivingtime, (nextevent.stop_number+1)%stopnumber, nextevent.bus_number);

						bus_position.add(new position(bnumber, snumber, clock, false));
					}
					else
					{

						bs[snumber].waitqueue--;
						if(min[snumber] >= bs[snumber].waitqueue)
							min[snumber] = bs[snumber].waitqueue;
						
						wqueue_length[nextevent.stop_number] = wqueue_length[nextevent.stop_number] 
								+ bs[nextevent.stop_number].waitqueue;
						stop_count[nextevent.stop_number]++;
						
						generateEvent("boarder", clock+boardingtime, snumber, nextevent.bus_number);
						
						//System.out.println("Time:" + clock + ", " + "one person get on bus_" + bnumber + " at stop_" + snumber);
					}
					break;
				}
			}//end switch
		
		}while(clock <= stoptime);
		System.out.println(bus_position.size());
	
		//use this function to find the bus's position 
		while(true)
		{
			System.out.println("Note:type -1 to exit!");
			int bn = 0;
			double tt = 0;
			System.out.println("Please insert which bus you want to see:");
			Scanner sc = new Scanner(System.in);
			bn = sc.nextInt();
			if(bn == -1)
				break;
			System.out.println("Please intsert the time:");
			sc = new Scanner(System.in);
			tt = sc.nextDouble();
			if(tt == -1)
				break;
			getBusPosition(bn, tt);
			System.out.println("");
		
		}
		
		
	}

}

class event
{
	 String type = "";
	 double time = 0;
	 int stop_number = 0;		//0,1,2,3,.....,14
	 int bus_number = 0;			//0,1,2,3,4
};

class busstop
{
	int waitqueue;
	int[] bus;
	busstop(int n1, int n2)
	{
		waitqueue = n1;
		bus = new int[n2];
	}
};

class position
{
	int bus_number;
	int stop_number;
	double time;
	boolean atstop;

	position(int n1, int n2, double n3,boolean n4)
	{
		bus_number = n1;
		stop_number = n2;
		time = n3;
		atstop = n4;
	}
};



