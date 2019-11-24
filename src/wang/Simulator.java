package wang;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Simulator {

	public static int seed;	
	public static double io_needed = 60;	//ms
	
	public static boolean CPU_isfree;
	public static boolean IO_isfree;
	public static job[] jobQ;
	public static double[] timeJG;
	public static ArrayList<job> ReadyQ;
	public static ArrayList<job> IOQ;
	public static ArrayList<Event2> eQ;
	public static double clock;
	//用于计算CPU利用率
	public static double CPU_startfree;
	public static double CPU_waitingtime;
	public static boolean flag;
	//用于计算吞吐量
	public static int CPUrequest_times;
	
	//用于计算每个job的总运行时间
	public static double[] finishtime;
	
	//用于计算每个job在IO里的时间
	public static double[] inIOtime;
	
	//用于SJF算法的预测,保存每个job长度的预测值和实际值
	public static double[] predictT;
	public static double[] actualT;
	
	//用于画GANTT图
	public static ArrayList<Integer> ids;
	public static ArrayList<Double> clocks;
	
	//用于测试
	public static double test;
	
	public static void init(int number)
	{
		seed = 1000;
		CPU_isfree = true;
		IO_isfree = true;
		jobQ = new job[number];					//记录所有10个job的信息
		timeJG = new double[number];			//记录每个job的IO请求时间间隔
		for(int i=0;i<number;i++)
		{
			jobQ[i] = new job(i,0,pRandom1(),"ready",0);
			timeJG[i] = 30.0 + i*5.0;
		}
		ReadyQ = new ArrayList<>();
		IOQ = new ArrayList<>();
		eQ = new ArrayList<>();
		clock = 0;
		
		CPU_startfree = 0;
		CPU_waitingtime = 0;
		flag = false;
		
		CPUrequest_times = 0;
		
		finishtime = new double[10];
		
		inIOtime = new double[10];
		
		predictT = new double[10];
		actualT = new double[10];
		
		ids = new ArrayList<>();
		clocks = new ArrayList<>();
		
		test = 0;
	}
	//FCFS算法(算法执行过程中给出作图参数)
	public static void FCFS()
	{
		int count = 0;
		while(!eQ.isEmpty())
		{
			count++;
			
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
			//f找到事件队列中发生事件最早的那个
			
			
			//1.如果是"arrivalCPU事件"
			if(eQ.get(index).type.equals("arrivalCPU"))
			{
				//更新系统时钟
				clock = eQ.get(index).time;
				//create a 	pcb
				job j_h = eQ.get(index).j;				
				
				if(CPU_isfree)
				{
					CPU_isfree = false;
					j_h.state = "running";
					//计算何时产生下一个什么事件
					double temp = pRandom2(timeJG[j_h.id]);
					//剩余执行时间是否大于要产生下一个IO请求所需要的时间
					//如果小于产生IO请求需要的时间，那么这个job直接结束，产生一个CPUfinish事件
					if(j_h.burst_time <= temp)
					{		
						
						double tt = clock + j_h.burst_time;
						j_h.burst_time = 0;
						
						//这个JOB剩下的时间为0，要结束它，并记录结束时间				
						eQ.add(new Event2("CPUfinish", j_h, tt));
						finishtime[j_h.id] = tt;
						
						//*******处理完一次CPU请求
						CPUrequest_times++;
						
						//**1**一个job出CPU，要画图********
						ids.add(j_h.id);
						clocks.add(tt);
						
						
//						System.out.println(j_h.id + "结束！    " + tt);
					
					}
					//如果时间够产生下一个IO请求，则产生下一个IOArrival事件和CPUfinish事件
					else
					{
					
						double tt = clock + temp;
						j_h.burst_time = j_h.burst_time - temp;
						eQ.add(new Event2("IOarrival", j_h, tt));
						eQ.add(new Event2("CPUfinish", j_h, tt));
						
						//**2**一个job出CPU进IO，要画图********
						ids.add(j_h.id);
						clocks.add(tt);
						
						//*******处理完一次CPU请求
						CPUrequest_times++;
					}
				}
				else
				{
					j_h.state = "ready";
					ReadyQ.add(j_h);
				}
				eQ.remove(index);				
			}
			else
				//2，如果是IOarrival事件
				if(eQ.get(index).type.equals("IOarrival"))
				{		
					//更新系统时钟
					clock = eQ.get(index).time;
					job j_h = eQ.get(index).j;
					//到达IO队列之后，检查IO是否可用
					//如果可用，60ms之后产生IOfinish和arrivalCPU事件
					if(IO_isfree)
					{
						IO_isfree = false;
						j_h.state = "IOrunning";
						eQ.add(new Event2("arrivalCPU", j_h, clock+60));
						eQ.add(new Event2("IOfinish", j_h, clock+60));
						//产生一个IO结束事件，该job的IO时间要增加
						inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
						
					}
					else
					{
						j_h.state = "IOready";
						IOQ.add(j_h);
					}
					eQ.remove(index);
					
				}
				else 
					//3.如果是CPUfinish事件
					if(eQ.get(index).type.equals("CPUfinish"))
					{
						//更新时钟
						clock = eQ.get(index).time;
						//要从readyQ里找下一个进CPU
					
						//如果readyQ不为空
						if(!ReadyQ.isEmpty())
						{
							//看之前CPU有没有等过，等过的话，要计算等待时间,并调整flag，表示上一次进来之前，CPU没有等过谁
							if(flag)
							{
								CPU_waitingtime = CPU_waitingtime + (clock - CPU_startfree);
								flag = false;
								
								//**3**一段空档时间free********
								ids.add(-1);
								clocks.add(clock + (clock - CPU_startfree - timeJG[eQ.get(index).j.id] - 20));
							}
							
							//找先来的那个，进入CPU
							job j_h = ReadyQ.get(0);
							ReadyQ.remove(0);
							j_h.state = "running";
							
//							System.out.print("     " + j_h.burst_time + "     ");
							
							//于arrivalCPU事件的处理方法一样
							double temp = pRandom2(timeJG[j_h.id]);
							if(j_h.burst_time <= temp)
							{				
						
								double tt = clock + j_h.burst_time;
								j_h.burst_time = 0;
								
								//一段时间后这个job彻底结束，记录结束时间		
								eQ.add(new Event2("CPUfinish", j_h, tt));
								finishtime[j_h.id] = tt;
//								System.out.println(j_h.id + "结束！" + tt);
								
								//**4**一个job出CPU，要画图********
								ids.add(j_h.id);
								clocks.add(tt);
								
							
								//*******处理完一次CPU请求
								CPUrequest_times++;
							}
							else
							{
						
								double tt = clock + temp;
								j_h.burst_time = j_h.burst_time - temp;
								eQ.add(new Event2("IOarrival", j_h, tt));
								eQ.add(new Event2("CPUfinish", j_h, tt));
								
								//**5**一个job出CPU进IO，要画图********
								ids.add(j_h.id);
								clocks.add(tt);
								
								
								//*******处理完一次CPU请求
								CPUrequest_times++;
							}
						}
						//如果readyQ队列为空，则没事做，CPU空闲
						else
						{
							CPU_isfree = true;
							//flag 变为true， 表示之前CPU开始等待， 记录此时的系统时间
							flag = true;
							CPU_startfree = clock;
							
						}
						eQ.remove(index);
					}
					else
						//4.如果是IOfinish事件
						if(eQ.get(index).type.equals("IOfinish"))
						{
							//更新时钟
							clock = eQ.get(index).time;
							job j_h = eQ.get(index).j;
							
				
							//还要去IO队列里找到下一个进IO
							//如果IO队列不空，60ms之后再产生和之前处理IOarrival相同的两个事件
							if(!IOQ.isEmpty()) 
							{
								j_h = IOQ.get(0);
								IOQ.remove(0);
								j_h.state = "IOrunning";
								eQ.add(new Event2("IOfinish", j_h, clock + 60));
								//产生一个IO结束事件，该job的IO时间要增加
								inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
								
								eQ.add(new Event2("arrivalCPU", j_h, clock + 60));
							}
							//如果IO队列为空，IO空闲
							else
								IO_isfree = true;
	
							eQ.remove(index);
							
						}
		}
	}
	//SJF算法
	public static void SJF(double alpha)
	{
		int count = 0;
		while(!eQ.isEmpty())
		{
			count++;		
			
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
			//f找到事件队列中发生事件最早的那个
						
			//1.如果是"arrivalCPU事件" ***(这里于FCFS完全相同，只是在添加到 readyQ的时候要重新调整一下位置)
			if(eQ.get(index).type.equals("arrivalCPU"))
			{
				//更新系统时钟
				clock = eQ.get(index).time;
				//create a 	pcb
				job j_h = eQ.get(index).j;				
				
				if(CPU_isfree)
				{
					CPU_isfree = false;
					j_h.state = "running";
					//计算何时产生下一个什么事件
					double temp = pRandom2(timeJG[j_h.id]);
					//剩余执行时间是否大于要产生下一个IO请求所需要的时间
					//如果小于产生IO请求需要的时间，那么这个job直接结束，产生一个CPUfinish事件
					if(j_h.burst_time <= temp)
					{	
						//更新这个job的实际结束时间
						actualT[j_h.id] = j_h.burst_time;
						
						double tt = clock + j_h.burst_time;
						j_h.burst_time = 0;
						
						//这个JOB剩下的时间为0，要结束它，并记录结束时间				
						eQ.add(new Event2("CPUfinish", j_h, tt));
						finishtime[j_h.id] = tt;
					
						//**1**一个job出CPU要画图********
						ids.add(j_h.id);
						clocks.add(tt);
						
						//*******处理完一次CPU请求
						CPUrequest_times++;
					
					}
					//如果时间够产生下一个IO请求，则产生下一个IOArrival事件和CPUfinish事件
					else
					{
						//更新这个job的实际结束时间
						actualT[j_h.id] = temp;
						
						double tt = clock + temp;
						j_h.burst_time = j_h.burst_time - temp;
						eQ.add(new Event2("IOarrival", j_h, tt));
						eQ.add(new Event2("CPUfinish", j_h, tt));
						
						//**2**一个job出CPU进IO，要画图********
						ids.add(j_h.id);
						clocks.add(tt);
						
						//*******处理完一次CPU请求
						CPUrequest_times++;
					}
				}
				//！！！！！！！！！！1如果CPU 正忙，要将这个job添加到readyQ中，并更新readyQ使他符合SJF算法
				else
				{
					j_h.state = "ready";
					ReadyQ.add(j_h);
					//---------------------------------插入readyQ之后要更新一下readyQ！把短的放在前面
					
					sortReadyQ(alpha);
					
					//---------------------------------
				}
				eQ.remove(index);				
			}
			else
				//2，如果是IOarrival事件
				if(eQ.get(index).type.equals("IOarrival"))
				{		
					//更新系统时钟
					clock = eQ.get(index).time;
					job j_h = eQ.get(index).j;
					//到达IO队列之后，检查IO是否可用
					//如果可用，60ms之后产生IOfinish和arrivalCPU事件
					if(IO_isfree)
					{
						IO_isfree = false;
						j_h.state = "IOrunning";
						eQ.add(new Event2("arrivalCPU", j_h, clock+60));
						eQ.add(new Event2("IOfinish", j_h, clock+60));
						//产生一个IO结束事件，该job的IO时间要增加
						inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
						
					}
					else
					{
						j_h.state = "IOready";
						IOQ.add(j_h);
					}
					eQ.remove(index);
					
				}
				else 
					//3.如果是CPUfinish事件
					if(eQ.get(index).type.equals("CPUfinish"))
					{
						//更新时钟
						clock = eQ.get(index).time;
						//要从readyQ里找下一个进CPU
					
						//如果readyQ不为空
						if(!ReadyQ.isEmpty())
						{
							//看之前CPU有没有等过，等过的话，要计算等待时间,并调整flag，表示上一次进来之前，CPU没有等过谁
							if(flag)
							{
								CPU_waitingtime = CPU_waitingtime + (clock - CPU_startfree);
								flag = false;
								
								//**3**一个job free，要画图********
								ids.add(-1);
								clocks.add(clock+ (clock - CPU_startfree) );
							}
							
							//找先来的那个，进入CPU
							job j_h = ReadyQ.get(0);
							ReadyQ.remove(0);
							j_h.state = "running";
							
//							System.out.print("     " + j_h.burst_time + "     ");
							
							//于arrivalCPU事件的处理方法一样
							double temp = pRandom2(timeJG[j_h.id]);
							if(j_h.burst_time <= temp)
							{		
								//更新这个job的实际结束时间
								actualT[j_h.id] = j_h.burst_time;
								
								double tt = clock + j_h.burst_time;
								j_h.burst_time = 0;
								
								//一段时间后这个job彻底结束，记录结束时间		
								eQ.add(new Event2("CPUfinish", j_h, tt));
								finishtime[j_h.id] = tt;
						
							
								//**4**一个job出CPU，要画图********
								ids.add(j_h.id);
								clocks.add(tt);
								
								//*******处理完一次CPU请求
								CPUrequest_times++;
							}
							else
							{
								//更新这个job的实际结束时间
								actualT[j_h.id] = temp;
								
								double tt = clock + temp;
								j_h.burst_time = j_h.burst_time - temp;
								eQ.add(new Event2("IOarrival", j_h, tt));
								eQ.add(new Event2("CPUfinish", j_h, tt));
								
								//**5**一个job出CPU进IO，要画图********
								ids.add(j_h.id);
								clocks.add(tt);
								
								//*******处理完一次CPU请求
								CPUrequest_times++;
							}
						}
						//如果readyQ队列为空，则没事做，CPU空闲
						else
						{
							CPU_isfree = true;
							//flag 变为true， 表示之前CPU开始等待， 记录此时的系统时间
							flag = true;
							CPU_startfree = clock;
							
						}
						eQ.remove(index);
					}
					else
						//4.如果是IOfinish事件
						if(eQ.get(index).type.equals("IOfinish"))
						{
							//更新时钟
							clock = eQ.get(index).time;
							job j_h = eQ.get(index).j;
							
				
							//还要去IO队列里找到下一个进IO
							//如果IO队列不空，60ms之后再产生和之前处理IOarrival相同的两个事件
							if(!IOQ.isEmpty()) 
							{
								j_h = IOQ.get(0);
								IOQ.remove(0);
								j_h.state = "IOrunning";
								eQ.add(new Event2("IOfinish", j_h, clock + 60));
								//产生一个IO结束事件，该job的IO时间要增加
								inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
								
								eQ.add(new Event2("arrivalCPU", j_h, clock + 60));
							}
							//如果IO队列为空，IO空闲
							else
								IO_isfree = true;
	
							eQ.remove(index);
							
						}
		}
	}
	
	//预测某一个job的长度, 并且更新readyQ,将最短的那个放在队列头
	public static void sortReadyQ(double alpha)
	{
		int index_h = 0;
		double min = alpha * actualT[0] + predictT[0]*(1-alpha);
		for(int i=0;i<ReadyQ.size();i++)
		{
			predictT[i] = alpha * actualT[i] + predictT[i]*(1-alpha);
			if(predictT[i] < min)
			{
				min = predictT[i];
				index_h = i;
			}
		}
		//找到了readyQ中，下一次长度最短的job的id，为index,将它放到readyQ的头
		job temp = ReadyQ.get(0);
		ReadyQ.set(0, ReadyQ.get(index_h));
		ReadyQ.set(index_h, temp);
	}
	
	//RR算法,时间片长度为quantum
	//区别在于每次要产生结束CPU事件的时候，会查询时间片
	public static void RR(double quantum)
	{
	
		while(!eQ.isEmpty())
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
			//f找到事件队列中发生事件最早的那个

			
			//1.如果是"arrivalCPU事件"
			if(eQ.get(index).type.equals("arrivalCPU"))
			{
				//更新系统时钟
				clock = eQ.get(index).time;
				//create a 	pcb
				job j_h = eQ.get(index).j;				
				
				if(CPU_isfree)
				{
					CPU_isfree = false;
					j_h.state = "running";
					//计算何时产生下一个IO事件
					double temp = pRandom2(timeJG[j_h.id]);
					
					//如果产生下一个IO请求的时间 < 时间片
					if(temp < quantum)
					{
						if(j_h.burst_time <= temp)
						{	
							// 如果剩余时间 <= 产生下一个IO请求的时间 < 时间片的时间， 这个进程burst后直接结束
							double tt = clock + j_h.burst_time;
							j_h.burst_time = 0;
							
							//这个JOB剩下的时间为0，要结束它，并记录结束时间				
							eQ.add(new Event2("CPUfinish", j_h, tt));
							finishtime[j_h.id] = tt;
							
							//*******处理完一次CPU请求
							CPUrequest_times++;
							
							//**1**一个job出CPU要画图********
							ids.add(j_h.id);
							clocks.add(tt);			
						}
						else 
							// 如果     剩余时间 > 时间片的时间 > 产生下一个IO请求的时间， 要产生IO事件
							// 或者     产生IO请求的时间  < 如果 剩余时间 <= 时间片的时间， 要去IO
							
							{
								double tt = clock + temp;
								j_h.burst_time = j_h.burst_time - temp;
								
								//产生IO事件
								
								eQ.add(new Event2("IOarrival", j_h, tt));
								eQ.add(new Event2("CPUfinish", j_h, tt));
								
								//*******处理完一次CPU请求
								CPUrequest_times++;			
								
								//**2**一个job出CPU，进IO要画图********
								ids.add(j_h.id);
								clocks.add(tt);
								
							}
					}
					
					
					// 如果产生下一个IO请求的时间 >= 时间片
					else
					{
						//如果 剩余时间 <= 时间片的时间 <= 产生下一个IO请求的时间, 这个job要结束
						if(j_h.burst_time <= quantum)
						{
							double tt = clock + j_h.burst_time;
							j_h.burst_time = 0;
							
							//这个JOB剩下的时间为0，要结束它，并记录结束时间				
							eQ.add(new Event2("CPUfinish", j_h, tt));
							finishtime[j_h.id] = tt;
							
							//*******处理完一次CPU请求
							CPUrequest_times++;
							
							//**3**一个job出CPU要画图********
							ids.add(j_h.id);
							clocks.add(tt);
						}
						else
						{
							//如果   剩余时间 >= 产生下一个IO请求的时间 >= 时间片的时间 ， 要产生时间片事件
							//     产生下一个IO请求的时间 > 剩余时间 >= 时间片的时间
				
							double tt = clock + quantum;
							j_h.burst_time = j_h.burst_time - quantum;
							
							//产生时间片事件
							
							eQ.add(new Event2("timer interrupt", j_h, tt));
					
							//*******处理完一次CPU请求
							CPUrequest_times++;			
							
							//**4**一个job出CPU要画图********
							ids.add(j_h.id);
							clocks.add(tt);
			
						}
					}
				}
				//如果CPU不free
				else
				{
					j_h.state = "ready";
					ReadyQ.add(j_h);
				}
				eQ.remove(index);				
			}
			else
				//2，如果是IOarrival事件
				if(eQ.get(index).type.equals("IOarrival"))
				{		
					//更新系统时钟
					clock = eQ.get(index).time;
					job j_h = eQ.get(index).j;
					//到达IO队列之后，检查IO是否可用
					//如果可用，60ms之后产生IOfinish和arrivalCPU事件
					if(IO_isfree)
					{
						IO_isfree = false;
						j_h.state = "IOrunning";
						eQ.add(new Event2("arrivalCPU", j_h, clock+60));
						eQ.add(new Event2("IOfinish", j_h, clock+60));
						//产生一个IO结束事件，该job的IO时间要增加
						inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
						
					}
					else
					{
						j_h.state = "IOready";
						IOQ.add(j_h);
					}
					eQ.remove(index);
					
				}
				else 
					//3.如果是CPUfinish事件
					if(eQ.get(index).type.equals("CPUfinish"))
					{
						//更新时钟
						clock = eQ.get(index).time;
						//要从readyQ里找下一个进CPU
					
						//如果readyQ不为空
						if(!ReadyQ.isEmpty())
						{
							//看之前CPU有没有等过，等过的话，要计算等待时间,并调整flag，表示上一次进来之前，CPU没有等过谁
							if(flag)
							{
								
								CPU_waitingtime = CPU_waitingtime + (clock - CPU_startfree);
								flag = false;
								
								//**5**一个job free 结束，C要画图********
								ids.add(-1);
								clocks.add(clock - CPU_waitingtime);
							}
							
							//找先来的那个，进入CPU
							job j_h = ReadyQ.get(0);
							ReadyQ.remove(0);
							j_h.state = "running";

							
							//*****找到一个Job j_h， 处理他的方法 与arrivalCPU事件的处理方法一样******
							
							double temp = pRandom2(timeJG[j_h.id]);
							
							//如果产生下一个IO请求的时间 < 时间片
							if(temp < quantum)
							{
								if(j_h.burst_time <= temp)
								{	
									// 如果剩余时间 <= 产生下一个IO请求的时间 < 时间片的时间， 这个进程burst后直接结束
									double tt = clock + j_h.burst_time;
									j_h.burst_time = 0;
									
									//这个JOB剩下的时间为0，要结束它，并记录结束时间				
									eQ.add(new Event2("CPUfinish", j_h, tt));
									finishtime[j_h.id] = tt;
									
									//*******处理完一次CPU请求
									CPUrequest_times++;

									//**6**一个job出CPU要画图********
									ids.add(j_h.id);
									clocks.add(tt);
								}
								else 
									// 如果     剩余时间 > 时间片的时间 > 产生下一个IO请求的时间， 要产生IO事件
									// 或者     产生IO请求的时间  < 如果 剩余时间 <= 时间片的时间， 要去IO
									
									{
										double tt = clock + temp;
										j_h.burst_time = j_h.burst_time - temp;
										
										//产生IO事件
										
										eQ.add(new Event2("IOarrival", j_h, tt));
										eQ.add(new Event2("CPUfinish", j_h, tt));
										
										//*******处理完一次CPU请求
										CPUrequest_times++;		
										
										//**7**一个job出CPU,进io,要画图********
										ids.add(j_h.id);
										clocks.add(tt);
										
									}
							}
							
							
							// 如果产生下一个IO请求的时间 >= 时间片
							else
							{
								//如果 剩余时间 <= 时间片的时间 <= 产生下一个IO请求的时间, 这个job要结束
								if(j_h.burst_time <= quantum)
								{
									double tt = clock + j_h.burst_time;
									j_h.burst_time = 0;
									
									//这个JOB剩下的时间为0，要结束它，并记录结束时间				
									eQ.add(new Event2("CPUfinish", j_h, tt));
									finishtime[j_h.id] = tt;
									
									//*******处理完一次CPU请求
									CPUrequest_times++;
									
									//**8**一个job出CPU要画图********
									ids.add(j_h.id);
									clocks.add(tt);
								
								}
								else
								{
									//如果   剩余时间 >= 产生下一个IO请求的时间 >= 时间片的时间 ， 要产生时间片事件
									//     产生下一个IO请求的时间 > 剩余时间 >= 时间片的时间
						
									double tt = clock + quantum;
									j_h.burst_time = j_h.burst_time - quantum;
									
									//产生时间片事件
									
									eQ.add(new Event2("timer interrupt", j_h, tt));
							
									//*******处理完一次CPU请求
									CPUrequest_times++;		
									
									//**9**一个job出CPU要画图********
									ids.add(j_h.id);
									clocks.add(tt);
					
								}
							}
							
						}
						//如果readyQ队列为空，则没事做，CPU空闲
						else
						{
							CPU_isfree = true;
							//flag 变为true， 表示之前CPU开始等待， 记录此时的系统时间
							flag = true;
							CPU_startfree = clock;
							
						}
						eQ.remove(index);
					}
					else
						//4.如果是IOfinish事件
						if(eQ.get(index).type.equals("IOfinish"))
						{
							//更新时钟
							clock = eQ.get(index).time;
							job j_h = eQ.get(index).j;
							
				
							//还要去IO队列里找到下一个进IO
							//如果IO队列不空，60ms之后再产生和之前处理IOarrival相同的两个事件
							if(!IOQ.isEmpty()) 
							{
								j_h = IOQ.get(0);
								IOQ.remove(0);
								j_h.state = "IOrunning";
								eQ.add(new Event2("IOfinish", j_h, clock + 60));
								//产生一个IO结束事件，该job的IO时间要增加
								inIOtime[j_h.id] = inIOtime[j_h.id] + 60.0; 
								
								eQ.add(new Event2("arrivalCPU", j_h, clock + 60));
							}
							//如果IO队列为空，IO空闲
							else
								IO_isfree = true;
	
							eQ.remove(index);
							
						}
						else
							//5.如果是timer interrupt事件
							if(eQ.get(index).type.equals("timer interrupt"))
							{
								//更新系统时钟
								clock = eQ.get(index).time;
								
								job j_h = eQ.get(index).j;
								//来之前， job的剩余时间已经改过了！
								j_h.state = "ready";
								ReadyQ.add(j_h);
								//将这个Job放到readyQ里面， 并取出readyQ中的一个Job,进行处理
								j_h = ReadyQ.get(0);
								ReadyQ.remove(0);
								j_h.state = "running";							
								
								//*****找到的这个Job j_h， 处理他的方法 与arrivalCPU事件的处理方法一样******
								
								double temp = pRandom2(timeJG[j_h.id]);
	
//								System.out.println("temp :" + temp + " ,quantum: " + quantum);
								
								//如果产生下一个IO请求的时间 < 时间片
								if(temp < quantum)
								{
									if(j_h.burst_time <= temp)
									{	
										// 如果剩余时间 <= 产生下一个IO请求的时间 < 时间片的时间， 这个进程burst后直接结束
										double tt = clock + j_h.burst_time;
										j_h.burst_time = 0;
										
										//这个JOB剩下的时间为0，要结束它，并记录结束时间				
										eQ.add(new Event2("CPUfinish", j_h, tt));
										finishtime[j_h.id] = tt;
										
										//*******处理完一次CPU请求
										CPUrequest_times++;
										
										//**10**一个job出CPU要画图********
										ids.add(j_h.id);
										clocks.add(tt);
																			
									}
									else 
										// 如果     剩余时间 > 时间片的时间 > 产生下一个IO请求的时间， 要产生IO事件
										// 或者     产生IO请求的时间  < 如果 剩余时间 <= 时间片的时间， 要去IO
										
										{
											double tt = clock + temp;
											j_h.burst_time = j_h.burst_time - temp;
											
											//产生IO事件
											
											eQ.add(new Event2("IOarrival", j_h, tt));
											eQ.add(new Event2("CPUfinish", j_h, tt));
											
											//*******处理完一次CPU请求
											CPUrequest_times++;			
											
											//**11**一个job出CPU要画图********
											ids.add(j_h.id);
											clocks.add(tt);
										}
								}
								
								
								// 如果产生下一个IO请求的时间 >= 时间片
								else
								{
									//如果 剩余时间 <= 时间片的时间 <= 产生下一个IO请求的时间, 这个job要结束
									if(j_h.burst_time <= quantum)
									{
										double tt = clock + j_h.burst_time;
										j_h.burst_time = 0;
										
										//这个JOB剩下的时间为0，要结束它，并记录结束时间				
										eQ.add(new Event2("CPUfinish", j_h, tt));
										finishtime[j_h.id] = tt;
										
										//*******处理完一次CPU请求
										CPUrequest_times++;
										
										//**12**一个job出CPU要画图********
										ids.add(j_h.id);
										clocks.add(tt);
									}
									else
									{
										//如果   剩余时间 >= 产生下一个IO请求的时间 >= 时间片的时间 ， 要产生时间片事件
										//     产生下一个IO请求的时间 > 剩余时间 >= 时间片的时间
							
										double tt = clock + quantum;
										j_h.burst_time = j_h.burst_time - quantum;
										
										//产生时间片事件
										
										eQ.add(new Event2("timer interrupt", j_h, tt));
								
										//*******处理完一次CPU请求
										CPUrequest_times++;			
										
										//**13**一个job出CPU要画图********
										ids.add(j_h.id);
										clocks.add(tt);
						
									}
								}
								eQ.remove(index);
							}
		}
	}
	
	
	//generate 2min-4min
	public static double pRandom1()
	{
		double x = 0;
		x = seed/65535.0;
		seed = (25173*seed + 13849)%65535;
		return (int)( (x*2+2)*60*1000 );
	}
	
	//generate average a
	public static double pRandom2(double a)
	{
		double x = 0;
		x = -a*Math.log((seed + 1.0)/65535.0);
		seed = (25173*seed + 13849) % 65535;
		return x;
	}
	
	//输出GANTT图函数
	public static void output() throws Exception
	{
		FileWriter fw = new FileWriter("logfile.dat");
		PrintWriter pw = new PrintWriter(fw);
		pw.println("start:");
		int k = 1;
		//先合并一些单元
		do 
		{
			if(ids.get(k-1) == ids.get(k))
			{
				ids.remove(k-1);
				clocks.remove(k-1);
				k--;
			}
			k++;
		}while(k<ids.size());
		
		//画图,第一行
		pw.print("|");
		for(int i=0;i<ids.size();i++)
		{
			//如果不是free
			if(ids.get(i) != -1)
			{
				pw.print(" " + ids.get(i) + " |");
			}
			else
			{
				pw.print("free|");
			}
		}
		pw.println("");
		
		//画图，第二行
		pw.print("0");
		for(int i=0;i<clocks.size();i++)
		{
			pw.print( " " + clocks.get(i) + " ");
		}
		pw.println("");
		pw.flush();
		pw.close();
		
		//屏幕上输出一部分，看效果
		
		//画图,第一行
		System.out.print("|");
		for(int i=0;i<20;i++)
		{
			//如果不是free
			if(ids.get(i) != -1)
			{
				System.out.print("     " + ids.get(i) + "     |");
			}
			else
			{
				System.out.print("   free   |");
			}
		}
		System.out.println("");
		
		//画图，第二行
		System.out.print("0");
		for(int i=0;i<20;i++)
		{
			double t = clocks.get(i);
			String s = String.format("%.2f", t);
			for(int j = 0;j<12-s.length();j++)
				System.out.print(" ");
			System.out.print(s);
		}
		System.out.println("");
		
	}
	
	
	//主函数
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		int jobnumber = 10;
		init(jobnumber);
		
		double totaltime_inCPU = 0;
		
		for(int i=0;i<jobnumber;i++)
		{	
			jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
			totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
			eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
		}
		//------------------------------------------------------------FCFS----------------------------------------------
		FCFS();
		System.out.println("FCFS GanTT:");
		output();
		//先计算CPU利用率
		double uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
		System.out.println("CPU utilization: " + uti + "%");
		
		//再计算吞吐量（总时间内处理的CPU请求个数）
		System.out.println("Throughput: " + CPUrequest_times);
	
		//再计算所有job的总的运行时间
		double totalturntime = 0;
		for(int i=0;i<10;i++)
		{
			totalturntime = totalturntime + finishtime[i];
		}
		totalturntime = totalturntime / 1000.0;
		System.out.println("Turnaround time: " + totalturntime + "s");
		
		//在计算所有job的总的等待时间
		double totalwaitingtime = 0;
		for(int i=0;i<10;i++)
		{
			double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
			totalwaitingtime = totalwaitingtime + wt;
		}
		totalwaitingtime = totalwaitingtime / 1000.0;
		System.out.println("Waiting time: " + totalwaitingtime + "s");
	
		
		//------------------------------------------------------------SJF 1----------------------------------------------
		init(jobnumber);
		totaltime_inCPU = 0;
		for(int i=0;i<jobnumber;i++)
		{	
			jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
			totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
			eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
		}
		double n = (double)1.0;
		SJF(n);
		System.out.println("SJF(" + n + "):" + "GanTT:");
		output();
		//先计算CPU利用率
		uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
		System.out.println("CPU utilization: " + uti + "%");
		
		//再计算吞吐量（总时间内处理的CPU请求个数）
		System.out.println("Throughput: " + CPUrequest_times);
	
		//再计算所有job的总的运行时间
		totalturntime = 0;
		for(int i=0;i<10;i++)
		{
			totalturntime = totalturntime + finishtime[i];
		}
		totalturntime = totalturntime / 1000.0;
		System.out.println("Turnaround time: " + totalturntime + "s");
		
		//在计算所有job的总的等待时间
		totalwaitingtime = 0;
		for(int i=0;i<10;i++)
		{
			double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
			totalwaitingtime = totalwaitingtime + wt;
		}
		totalwaitingtime = totalwaitingtime / 1000.0;
		System.out.println("Waiting time: " + totalwaitingtime + "s");
	
		//------------------------------------------------------------SJF 1/2----------------------------------------------
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				n = (double)1.0/2.0;
				SJF(n);
				System.out.println("SJF(1/2):" + "GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
			
				//------------------------------------------------------------SJF 1/3----------------------------------------------
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				n = (double)1.0/3.0;
				SJF(n);
				System.out.println("SJF(1/3):" + "GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
		
				
				//------------------------------------------------------------RR 20----------------------------------------------
				
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				int nn = 20;
				RR(nn);
				System.out.println("RR(" + nn + ") GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
//------------------------------------------------------------RR 25----------------------------------------------
				
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				nn = 25;
				RR(nn);
				System.out.println("RR(" + nn + ") GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
				
//------------------------------------------------------------RR 30----------------------------------------------
				
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				nn = 30;
				RR(nn);
				System.out.println("RR(" + nn + ") GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
				
//------------------------------------------------------------RR 35----------------------------------------------
				
				init(jobnumber);
				totaltime_inCPU = 0;
				for(int i=0;i<jobnumber;i++)
				{	
					jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
					totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
					eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
				}
				nn = 35;
				RR(nn);
				System.out.println("RR(" + nn + ") GanTT:");
				output();
				//先计算CPU利用率
				uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
				System.out.println("CPU utilization: " + uti + "%");
				
				//再计算吞吐量（总时间内处理的CPU请求个数）
				System.out.println("Throughput: " + CPUrequest_times);
			
				//再计算所有job的总的运行时间
				totalturntime = 0;
				for(int i=0;i<10;i++)
				{
					totalturntime = totalturntime + finishtime[i];
				}
				totalturntime = totalturntime / 1000.0;
				System.out.println("Turnaround time: " + totalturntime + "s");
				
				//在计算所有job的总的等待时间
				totalwaitingtime = 0;
				for(int i=0;i<10;i++)
				{
					double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
					totalwaitingtime = totalwaitingtime + wt;
				}
				totalwaitingtime = totalwaitingtime / 1000.0;
				System.out.println("Waiting time: " + totalwaitingtime + "s");
		//------------------------------------------------------------RR 40----------------------------------------------
	
		init(jobnumber);
		totaltime_inCPU = 0;
		for(int i=0;i<jobnumber;i++)
		{	
			jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
			totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
			eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
		}
		nn = 40;
		RR(nn);
		System.out.println("RR(" + nn + ") GanTT:");
		output();
		//先计算CPU利用率
		uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
		System.out.println("CPU utilization: " + uti + "%");
		
		//再计算吞吐量（总时间内处理的CPU请求个数）
		System.out.println("Throughput: " + CPUrequest_times);
	
		//再计算所有job的总的运行时间
		totalturntime = 0;
		for(int i=0;i<10;i++)
		{
			totalturntime = totalturntime + finishtime[i];
		}
		totalturntime = totalturntime / 1000.0;
		System.out.println("Turnaround time: " + totalturntime + "s");
		
		//在计算所有job的总的等待时间
		totalwaitingtime = 0;
		for(int i=0;i<10;i++)
		{
			double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
			totalwaitingtime = totalwaitingtime + wt;
		}
		totalwaitingtime = totalwaitingtime / 1000.0;
		System.out.println("Waiting time: " + totalwaitingtime + "s");
	
		//------------------------------------------------------------RR 60----------------------------------------------
		
			init(jobnumber);
			totaltime_inCPU = 0;
			for(int i=0;i<jobnumber;i++)
			{	
				jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
				totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
				eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
			}
			nn = 60;
			RR(nn);
			System.out.println("RR(" + nn + ") GanTT:");
			output();
			//先计算CPU利用率
			uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
			System.out.println("CPU utilization: " + uti + "%");
			
			//再计算吞吐量（总时间内处理的CPU请求个数）
			System.out.println("Throughput: " + CPUrequest_times);
		
			//再计算所有job的总的运行时间
			totalturntime = 0;
			for(int i=0;i<10;i++)
			{
				totalturntime = totalturntime + finishtime[i];
			}
			totalturntime = totalturntime / 1000.0;
			System.out.println("Turnaround time: " + totalturntime + "s");
			
			//在计算所有job的总的等待时间
			totalwaitingtime = 0;
			for(int i=0;i<10;i++)
			{
				double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
				totalwaitingtime = totalwaitingtime + wt;
			}
			totalwaitingtime = totalwaitingtime / 1000.0;
			System.out.println("Waiting time: " + totalwaitingtime + "s");
			
			//------------------------------------------------------------RR 65----------------------------------------------
			
			init(jobnumber);
			totaltime_inCPU = 0;
			for(int i=0;i<jobnumber;i++)
			{	
				jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
				totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
				eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
			}
			nn = 65;
			RR(nn);
			System.out.println("RR(" + nn + ") GanTT:");
			output();
			//先计算CPU利用率
			uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
			System.out.println("CPU utilization: " + uti + "%");
			
			//再计算吞吐量（总时间内处理的CPU请求个数）
			System.out.println("Throughput: " + CPUrequest_times);
		
			//再计算所有job的总的运行时间
			totalturntime = 0;
			for(int i=0;i<10;i++)
			{
				totalturntime = totalturntime + finishtime[i];
			}
			totalturntime = totalturntime / 1000.0;
			System.out.println("Turnaround time: " + totalturntime + "s");
			
			//在计算所有job的总的等待时间
			totalwaitingtime = 0;
			for(int i=0;i<10;i++)
			{
				double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
				totalwaitingtime = totalwaitingtime + wt;
			}
			totalwaitingtime = totalwaitingtime / 1000.0;
			System.out.println("Waiting time: " + totalwaitingtime + "s");
			
			//------------------------------------------------------------RR 70----------------------------------------------
			
			init(jobnumber);
			totaltime_inCPU = 0;
			for(int i=0;i<jobnumber;i++)
			{	
				jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
				totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
				eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
			}
			nn = 70;
			RR(nn);
			System.out.println("RR(" + nn + ") GanTT:");
			output();
			//先计算CPU利用率
			uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
			System.out.println("CPU utilization: " + uti + "%");
			
			//再计算吞吐量（总时间内处理的CPU请求个数）
			System.out.println("Throughput: " + CPUrequest_times);
		
			//再计算所有job的总的运行时间
			totalturntime = 0;
			for(int i=0;i<10;i++)
			{
				totalturntime = totalturntime + finishtime[i];
			}
			totalturntime = totalturntime / 1000.0;
			System.out.println("Turnaround time: " + totalturntime + "s");
			
			//在计算所有job的总的等待时间
			totalwaitingtime = 0;
			for(int i=0;i<10;i++)
			{
				double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
				totalwaitingtime = totalwaitingtime + wt;
			}
			totalwaitingtime = totalwaitingtime / 1000.0;
			System.out.println("Waiting time: " + totalwaitingtime + "s");
			
		
			//------------------------------------------------------------RR 75----------------------------------------------
			
			init(jobnumber);
			totaltime_inCPU = 0;
			for(int i=0;i<jobnumber;i++)
			{	
				jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
				totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
				eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
			}
			nn = 75;
			RR(nn);
			System.out.println("RR(" + nn + ") GanTT:");
			output();
			//先计算CPU利用率
			uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
			System.out.println("CPU utilization: " + uti + "%");
			
			//再计算吞吐量（总时间内处理的CPU请求个数）
			System.out.println("Throughput: " + CPUrequest_times);
		
			//再计算所有job的总的运行时间
			totalturntime = 0;
			for(int i=0;i<10;i++)
			{
				totalturntime = totalturntime + finishtime[i];
			}
			totalturntime = totalturntime / 1000.0;
			System.out.println("Turnaround time: " + totalturntime + "s");
			
			//在计算所有job的总的等待时间
			totalwaitingtime = 0;
			for(int i=0;i<10;i++)
			{
				double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
				totalwaitingtime = totalwaitingtime + wt;
			}
			totalwaitingtime = totalwaitingtime / 1000.0;
			System.out.println("Waiting time: " + totalwaitingtime + "s");
	

			
//------------------------------------------------------------RR 500----------------------------------------------
			
			init(jobnumber);
			totaltime_inCPU = 0;
			for(int i=0;i<jobnumber;i++)
			{	
				jobQ[i] = new job(i, 0, pRandom1(), "ready", 0);
				totaltime_inCPU = totaltime_inCPU + jobQ[i].burst_time;
				eQ.add(new Event2("arrivalCPU", jobQ[i], 0));
			}
			nn =500;
			RR(nn);
			System.out.println("RR(" + nn + ") GanTT:");
			output();
			//先计算CPU利用率
			uti = (double)100.0 * totaltime_inCPU / (totaltime_inCPU+CPU_waitingtime); 
			System.out.println("CPU utilization: " + uti + "%");
			
			//再计算吞吐量（总时间内处理的CPU请求个数）
			System.out.println("Throughput: " + CPUrequest_times);
		
			//再计算所有job的总的运行时间
			totalturntime = 0;
			for(int i=0;i<10;i++)
			{
				totalturntime = totalturntime + finishtime[i];
			}
			totalturntime = totalturntime / 1000.0;
			System.out.println("Turnaround time: " + totalturntime + "s");
			
			//在计算所有job的总的等待时间
			totalwaitingtime = 0;
			for(int i=0;i<10;i++)
			{
				double wt = finishtime[i] - inIOtime[i] - jobQ[i].burst_time;
				totalwaitingtime = totalwaitingtime + wt;
			}
			totalwaitingtime = totalwaitingtime / 1000.0;
			System.out.println("Waiting time: " + totalwaitingtime + "s");
	
	}

}













class job
{
	int id;
	double arrival_time;
	double burst_time;
	String state;
	int prio;
	
	job(int id, double at, double bt, String state,int prio)
	{
		this.id = id;
		this.arrival_time = at;
		this.burst_time = bt;
		this.state = state;
		this.prio = prio;
	}
}

class Event2
{
	String type;
	job j;
	double time;
	
	Event2(String ty, job j, double time)
	{
		this.type = ty;
		this.j = j;
		this.time = time;
	}
}