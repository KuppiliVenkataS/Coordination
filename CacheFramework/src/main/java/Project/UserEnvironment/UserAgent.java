package project.UserEnvironment;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Iterator;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import project.QueryEnvironment.Query_Subj_Predicate;

/**
 * Created by santhilata on 12/02/15.
 * 1. This class takes the Query and adds it's address sends it to QueryAnalysisAgent
 * 2.
 *  This user agent creates a query analysis agent.
 *  User agent sends query from query_execution time to the query analysis agent
 *  Starts  execution time for that query.
 *  Receives reply from Query Analysis agent and
 *  user agent dies after receiving the reply.

 */
public class UserAgent extends Agent {

    private String myQueryAgent;
    private Query_ExecutionTime query_time;
    private AgentController t1 = null;
    private static int i=0;

    private AID globalClockProvider;
    long userStartTime;
    long userEndTime;

    SequentialBehaviour setStartTimeBehaviour;
    SequentialBehaviour setEndTimeBehaviour;

    long systemStartTime,systemEndTime;
    MessageTemplate mt1;

    public Query_ExecutionTime getQuery_time() {
        return query_time;
    }
    public void setQuery_time(Query_ExecutionTime query_time) {
        this.query_time = query_time;
    }

 //    long currentTime;
    @Override
    protected void setup() {
        super.setup();

       // System.out.println(getLocalName() + " user Agent is setting up");
        // create the agent description of itself
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd  = new ServiceDescription();
        sd.setType( "User" );
        sd.setName( getLocalName() );
        dfd.addServices(sd);

        globalClockProvider = searchGlobalClock(this);
        systemStartTime = System.currentTimeMillis();

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId("GlobalTime");
        msg.addReceiver(globalClockProvider);
        msg.setContent("Current Time Please ");
        send(msg);       // this sends message for current time to Global clock. reply is received
        mt1 = MessageTemplate.and(MessageTemplate.MatchConversationId("GlobalTime"),
                MessageTemplate.MatchInReplyTo(msg.getReplyWith()));


        setStartTimeOfUA(); // to receive reply from Global clock

       // setStartTime();

        Object[] obj = new Object[2]; //first object is the query and second object is the user agent
        Object[] args = this.getArguments();
        if (args != null && args.length > 0) {

//            Query_Subj_Predicate qsp = (Query_Subj_Predicate) args[0];
//            Query_ExecutionTime qet = new Query_ExecutionTime(qsp);
            Query_ExecutionTime qet = (Query_ExecutionTime)args[0];

            this.setQuery_time(qet);    // no start or end time here

            obj[0] = qet;
            /*to  print query*/
             Uid_query();

           addBehaviour(new CreateQueryAgent(obj));

           addBehaviour(new TickerBehaviour(this,10000) {
               @Override
               protected void onTick() {
                 //  System.out.println("INSIDE TICKER BEHAVIOUR OF USER AGENT WAITING TO FINISH");
                  ACLMessage msg = receive();
                   if (msg != null)
                   if (msg.getContent().equals("Done")){
                      // sendMessage(msg.getSender());
                       setEndTime(myAgent);

                   }
               }
           });

        }

        else{
            System.out.println("Seems no Queries");
            doDelete();
        }

        // Adding  user Agent from whom this query agent has been generated
        Object obj_Agent = this;
        obj[1] = obj_Agent;

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    //this method is for debugging and get to know what query has been sent by the user agent
    public void Uid_query(){
       System.out.println("UID is: " + getLocalName());
       this.getQuery_time().getQuery().printQuery();
    }



    public void setMyQueryAgent(String myQueryAgent) {
        this.myQueryAgent = myQueryAgent;
    }


    /**
     * This class is only to create a query agent
     * and attach the query to query agent
     */
    private class CreateQueryAgent extends Behaviour{

        private Object[] objects;

        public CreateQueryAgent(Object[] objects){
            this.objects = objects;
        }

        @Override
        public void action() {
            String agentName ="QA"+getLocalName();
            setMyQueryAgent(agentName);

            try {
                AgentContainer container = (AgentContainer)getContainerController(); // get a container controller for creating new agents
              //  t1 = container.createNewAgent(agentName, "project.MiddlewareEnvironment.QueryAgent", null);
                t1 = container.createNewAgent(agentName, "project.MiddlewareEnvironment.QueryAgent", objects);
                t1.start();
             //   System.out.println(getLocalName()+" CREATED AND STARTED NEW QUERY AGENT:"+agentName + " ON CONTAINER "+container.getContainerName());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public boolean done() {

            return true;
        }
    }//create CreateQueryAgent

    /**
     * This method is to search for once the Global clock provider
     * @param myAgent
     * @return
     */
    public AID searchGlobalClock(Agent myAgent) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType("GlobalClock");
        template.addServices(templateSd);
        SearchConstraints sc = new SearchConstraints();
        // We want to receive 10 results at most
        sc.setMaxResults(new Long(10));
        DFAgentDescription[] results = null;
        AID provider = null;

        try {
            results = DFService.search(myAgent, template, sc);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        ACLMessage msg = null;

        if (results.length > 0) {
          //  System.out.println("Agent " + getLocalName() + " found the following clock services:");
            for (int i = 0; i < results.length; ++i) {
                DFAgentDescription dfd = results[i];
                 provider = dfd.getName();
                // The same agent may provide several services; we are only interested in Global Clock

                Iterator it = dfd.getAllServices();
                while (it.hasNext()) {
                    ServiceDescription sd = (ServiceDescription) it.next();

                    if (sd.getType().equals("GlobalClock")) {
                      //  System.out.println("- Service \"" + sd.getName() + "\" provided by agent " + provider.getName());

                    }
                }
            }
        }//if (results.length > 0 )

        return  provider;
    }    // search Global time


    /**
     * Sets start time of the query agent
     */
    public void setStartTimeOfUA(){
        MessageTemplate mt = null;
        ACLMessage reply = this.blockingReceive(mt);

        if (reply != null)
        {
            // Reply received
            if (reply.getPerformative() == ACLMessage.PROPOSE) {
                //   System.out.println("for starttime reply is " + reply.getContent() + " from " + reply.getSender()+" "+getName());
                userStartTime = Long.parseLong(reply.getContent());
               // System.out.println("User Agent: "+getName()+" Start time is :  "+userStartTime);
            }
        }
    }

    //below one is not working

    public void setStartTime(){
        setStartTimeBehaviour = new SequentialBehaviour(this);
        setStartTimeBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setConversationId("GlobalTime");
                msg.addReceiver(globalClockProvider);
                msg.setContent("Current Time Please ");
                send(msg);
                final MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("GlobalTime"),
                        MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
            }
        });
        setStartTimeBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = null;
                ACLMessage reply = myAgent.receive(mt);

                if (reply != null)
                {
                    // Reply received
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        //   System.out.println("for starttime reply is " + reply.getContent() + " from " + reply.getSender()+" "+getName());

                        userStartTime = Long.parseLong(reply.getContent());
                        //  System.out.println("CurrentTime is:" + currentTime);
                        // userStartTime = currentTime;
                        // System.out.println("Current time before : "+currentTime);
                        System.out.println("User "+getAID()+" Start time is :  "+userStartTime);
                    }
                }

                //   block();
            }
        });
    }

    public  void setEndTime(Agent myAgent) {

        setEndTimeBehaviour = new SequentialBehaviour(this);
        setEndTimeBehaviour.addSubBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setConversationId("GlobalTime");
                msg.addReceiver(globalClockProvider);
                msg.setContent("Current Time Please ");
                send(msg);
               final MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("GlobalTime"),  MessageTemplate.MatchInReplyTo(msg.getReplyWith()));
            }
        });


        //setEndTimeBehaviour.addSubBehaviour(new CyclicBehaviour() {
        setEndTimeBehaviour.addSubBehaviour(new TickerBehaviour(this,250) {
            @Override
            protected void onTick() {
           // public void action() {
                MessageTemplate mt = null;

                ACLMessage reply = myAgent.receive(mt);

                if (reply != null)
                {
                    // Reply received
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                      //  System.out.println("for endtime reply is " + reply.getContent() + " from " + reply.getSender()+" "+getName());

                        userEndTime = Long.parseLong(reply.getContent());
                      //  System.out.println("User "+getAID()+" End time is :  "+userEndTime);
                        query_time.compareTo(userEndTime);// set query end time this execution time

                        systemEndTime = System.currentTimeMillis();

                      //  System.out.println("Time spent user is "+getName()+" "+ (userEndTime-userStartTime));
                      //  System.out.println("Time spent System is "+getName()+" "+ (systemEndTime-systemStartTime));
                       doDelete();
                    }
                }
               // else System.out.println("88888888888888888888888888888888888888888888888888888888888888");

                block();
            }


        });

        addBehaviour(setEndTimeBehaviour);
    }

    @Override
    protected void takeDown(){
      //  setEndTime(this);
      //  System.out.println("User agent: "+getAID()+" is terminating");
      //  System.out.println("Time spent with User "+getName()+" : "+ userEndTime+ " "+userStartTime +" "+(userEndTime-userStartTime));
    }


}//end of class UserAgent
