package wang;

import java.util.ArrayList;

public class GanttChart {

	public static boolean CPU_isfree;
	public static ArrayList<PCB> waitQ; 
	public static ArrayList<Event1> eQ;
	public static ArrayList<PCB> outQ;
	public static ArrayList<Double> timeQ;
	public static double clock;
	
	public static void init()
	{
		CPU_isfree = true;
		waitQ = new ArrayList<PCB>();
		eQ = new ArrayList<Event1>(); 
		outQ = new ArrayList<PCB>();
		timeQ = new ArrayList<Double>();
		clock = 0;
	}
	
	public static void addEvent(Event1 e)
	{
		eQ.add(e);
	}
	
	public static void FCFS()
	{
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			double min = eQ.get(index).time;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).time < min)
				{	
					min = eQ.get(i).time;
					index = i;
				}
			}
			//find the min
//			System.out.println( " < "+ min + ">");
			
			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
					addEvent(e_h);
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						p_h = waitQ.get(0);
						waitQ.remove(0);
						//schedule completion
						addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
		}
		
	}
	
	public static void SJF()
	{
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			double min = eQ.get(index).time;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).time < min)
				{	
					min = eQ.get(i).time;
					index = i;
				}
			}
			//find the short			
			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
					addEvent(e_h);
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						int index2 = 0;
						double minb = waitQ.get(index2).burst_time;
						for(int j=0;j<waitQ.size();j++)
						{
							if(waitQ.get(j).burst_time <= minb)
							{
								minb = waitQ.get(j).burst_time;
								index2 = j;
							}
						}
						//find the short
						
						p_h = waitQ.get(index2);
						waitQ.remove(index2);
						//schedule completion
						addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
		}
	}
	public static void SJF2()
	{
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			double min = eQ.get(index).p.burst_time;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).p.burst_time < min)
				{	
					min = eQ.get(i).p.burst_time;
					index = i;
				}
			}
			//find the short			
			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
					addEvent(e_h);
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						int index2 = 0;
						double minb = waitQ.get(index2).burst_time;
						for(int j=0;j<waitQ.size();j++)
						{
							if(waitQ.get(j).burst_time <= minb)
							{
								minb = waitQ.get(j).burst_time;
								index2 = j;
							}
						}
						//find the short
						
						p_h = waitQ.get(index2);
						waitQ.remove(index2);
						//schedule completion
						addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
		}
	}
	public static void SJF_plus()
	{
		clock = 1;
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			double min = eQ.get(index).p.burst_time;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).p.burst_time <= min)
				{	
					min = eQ.get(i).p.burst_time;
					index = i;
				}
			}
			//find the short			
			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
//					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
					addEvent(e_h);
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						int index2 = 0;
						double minb = waitQ.get(index2).burst_time;
						for(int j=0;j<waitQ.size();j++)
						{
							if(waitQ.get(j).burst_time <= minb)
							{
								minb = waitQ.get(j).burst_time;
								index2 = j;
							}
						}
						//find the short
						
						p_h = waitQ.get(index2);
						waitQ.remove(index2);
						//schedule completion
						addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
		}
	}

	public static void NP()
	{
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			int min = eQ.get(index).p.prio;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).p.prio < min)
				{	
					min = eQ.get(i).p.prio;
					index = i;
				}
			}
			//find the min

			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
					addEvent(e_h);
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						int index2 = 0;
						int min2 = waitQ.get(index2).prio;
						for(int i=0;i<waitQ.size();i++)
							if(waitQ.get(i).prio < min2)
							{
								index2 = i;
								min2 = waitQ.get(i).prio;
							}
						
						p_h = waitQ.get(index2);
						waitQ.remove(index2);
						//schedule completion
						addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
		}
		
	}
	
	public static void RR(int q)
	{
		while(!eQ.isEmpty() )
		{
			
			int index = 0;
			double min = eQ.get(index).time;
			for(int i=0;i<eQ.size();i++)
			{
				if(eQ.get(i).time < min)
				{	
					min = eQ.get(i).time;
					index = i;
				}
			}
			//find the min
//			System.out.println( " < "+ min + ">");
			
			
			if(eQ.get(index).type.equals("arrival"))
			{
				
				//create a 	pcb
				PCB p_h = eQ.get(index).p;
				
				
				if(CPU_isfree)
				{
					clock = clock + p_h.arrival_time;
					CPU_isfree = false;
					p_h.state = "running";
					//schedule completion
					if(p_h.burst_time <= q)
					{	
						Event1 e_h = new Event1("completion", p_h, clock + p_h.burst_time);
						addEvent(e_h);
					}
					else
					{
						
						Event1 e_h = new Event1("interrupt", p_h, clock + q);
						addEvent(e_h);
					}
					
				}
				else
				{
					p_h.state = "ready";
					waitQ.add(p_h);
				}
				eQ.remove(index);
				
			}
			else
				if(eQ.get(index).type.equals("completion"))
				{				
					//destroy the PCB
					PCB p_h = eQ.get(index).p; 
					clock = eQ.get(index).time;
					
					outQ.add(p_h);
					timeQ.add(clock);

					eQ.remove(index);
						
					
					if( !waitQ.isEmpty() )
					{
						//take next PCB from queue
						p_h = waitQ.get(0);
						waitQ.remove(0);
						//schedule completion
						if(p_h.burst_time <=q && p_h.burst_time>0)
						{
							addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
						}
						else if(p_h.burst_time>q)
						{
							addEvent(new Event1("interrupt", p_h, clock + q));
							waitQ.add(p_h);
						}
					}
					else
					{
//						System.out.println("here");
						CPU_isfree = true;
					} 
				}
				else
					if(eQ.get(index).type.equals("interrupt"))
					{	
						PCB p_h = eQ.get(index).p; 
						eQ.remove(index);
						
						clock = clock + q;
						p_h.state = "ready";
						p_h.burst_time = p_h.burst_time - q;
						
						outQ.add(p_h);
						timeQ.add(clock);
						
						waitQ.add(p_h);
						p_h = waitQ.get(0);
						waitQ.remove(0);
						
						if(p_h.burst_time <= q  && p_h.burst_time>0)
						{						
							addEvent(new Event1("completion", p_h, clock + p_h.burst_time));
							
						}
						else if(p_h.burst_time>q)
						{
								addEvent(new Event1("interrupt", p_h, clock + q));
						}
						
					}
		}
		
	}
	
	public static void output1()
	{
		System.out.println("Gantt Chart:");
		System.out.println("-------------------------------------");
		System.out.print("|");
		int count = 11;
		for(int i=0;i<outQ.size();i++)
			System.out.print("     P" + outQ.get(i).id + "    |");
		System.out.println("");
		System.out.println("-------------------------------------");
		System.out.print("0.0");
		for(int i=0;i<timeQ.size();i++)
		{
			for(int j=0;j<count-2;j++)
				System.out.print(" ");
			System.out.print(timeQ.get(i) );
		}
	}
	public static void output1_plus()
	{
		System.out.println("Gantt Chart:");
		System.out.println("-------------------------------------------");
		System.out.print("|     |");
		int count = 11;
		for(int i=0;i<outQ.size();i++)
			System.out.print("     P" + outQ.get(i).id + "    |");
		System.out.println("");
		System.out.println("-------------------------------------------");
		System.out.print("0.0   1.0");
		for(int i=0;i<timeQ.size();i++)
		{
			for(int j=0;j<count-2;j++)
				System.out.print(" ");
			System.out.print(timeQ.get(i) );
		}
	}
	
	public static void output2()
	{
		System.out.println("Gantt Chart:");
		System.out.println("--------------------------------------------------------------");
		System.out.print("|");
		int count = 11;
		for(int i=0;i<outQ.size();i++)
			System.out.print("     P" + outQ.get(i).id + "    |");
		System.out.println("");
		System.out.println("--------------------------------------------------------------");
		System.out.print("0.0");
		for(int i=0;i<timeQ.size();i++)
		{
			for(int j=0;j<count-i-1;j++)
				System.out.print(" ");
			System.out.print(timeQ.get(i) );
		}
	}
	public static void output3()
	{
		System.out.println("Gantt Chart:");
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.print("|");
		for(int i=0;i<outQ.size();i++)
		{	if(i!=7&&i!=9&&i!=12&&i!=13&&i<23&&i!=17)
			System.out.print("   P" + outQ.get(i).id + "  |");
			if(i==17) System.out.print("   P" + (outQ.get(i).id+4) + "  |");}
		System.out.println("");
		System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.print("0.0");
		int count = 0;
		for(int i=0;i<timeQ.size();i++)
		{
			if(i!=7&&i!=9&&i!=12&&i!=13) {
			if(i<7)
				System.out.print("     "+timeQ.get(i) );
			else if(i==8)
				System.out.print("    "+timeQ.get(i-1) );
			else if(i>9 && i<12)
				System.out.print("     "+timeQ.get(i-2) );
			else if(i>13 && i<23)
				System.out.print("    "+timeQ.get(i-4) );
		}}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//problem 5.3
		init();
		PCB p1 = new PCB(1, 0.0, 8, null, 0);
		PCB p2 = new PCB(2, 0.4, 4, null, 0);
		PCB p3 = new PCB(3, 1.0, 1, null, 0);
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		System.out.println("5.3:");
		System.out.println("a)FCFS");
		FCFS();
		output1();
		System.out.println("");
		
		
		init();
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		System.out.println("b)SJF");
		SJF();
		output1();
		System.out.println("");
		
		init();
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		System.out.println("c)SJF_plus");
		SJF_plus();
		output1_plus();
		System.out.println("");
		System.out.println("");
		
		//problem 5.12
		init();
			p1 = new PCB(1, 0.0, 10, null, 3);
			p2 = new PCB(2, 0.0, 1, null, 1);
			p3 = new PCB(3, 0.0, 2, null, 3);
		PCB p4 = new PCB(4, 0.0, 1, null, 4);
		PCB p5 = new PCB(5, 0.0, 5, null, 2);
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		addEvent(new Event1("arrival", p4, p4.arrival_time));
		addEvent(new Event1("arrival", p5, p5.arrival_time));
		System.out.println("5.12:");
		System.out.println("FCFS:");
		FCFS();
		output2();
		System.out.println("");
		
		init();
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		addEvent(new Event1("arrival", p4, p4.arrival_time));
		addEvent(new Event1("arrival", p5, p5.arrival_time));
		System.out.println("5.12:");
		System.out.println("SJF:");
		SJF2();
		output2();
		System.out.println("");
	
		init();
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		addEvent(new Event1("arrival", p4, p4.arrival_time));
		addEvent(new Event1("arrival", p5, p5.arrival_time));
		System.out.println("5.12:");
		System.out.println("NonpreemptivePriority:");
		NP();
		output2();
		System.out.println("");
		
		init();
		addEvent(new Event1("arrival", p1, p1.arrival_time));
		addEvent(new Event1("arrival", p2, p2.arrival_time));
		addEvent(new Event1("arrival", p3, p3.arrival_time));
		addEvent(new Event1("arrival", p4, p4.arrival_time));
		addEvent(new Event1("arrival", p5, p5.arrival_time));
		System.out.println("5.12:");
		System.out.println("RR:");
		RR(1);
		output3();
		System.out.println("");
		
	}

}

class PCB
{
	int id;
	double arrival_time;
	double burst_time;
	String state;
	int prio;
	
	PCB(int id, double at, double bt, String state,int prio)
	{
		this.id = id;
		this.arrival_time = at;
		this.burst_time = bt;
		this.state = state;
		this.prio = prio;
	}
}

class Event1
{
	String type;
	PCB p;
	double time;
	
	Event1(String ty, PCB p, double time)
	{
		this.type = ty;
		this.p = p;
		this.time = time;
	}
	
	void setTime(double time)
	{
		this.time = time;
	}
}