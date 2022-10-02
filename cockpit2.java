package com.Arachnid;
import sun.font.CStrike;

import javax.sound.midi.Soundbank;
import java.awt.*;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class cockpit2 {
    //creating a node class I will implement later for searches
    static class Node {
        private String state;
        private Node parent;
        private List<List<String>> flights;
        private String airline;


        //let the state be the IATA
        // parent be the airport it came from
        // flights could be a list with possible airlines and destinations
        // let airline be the airline from the parent to the child
        public Node(String state, Node parent,String airline,  List<List<String>> flights) {
            this.state = state;
            this.parent = parent;
            this.airline = airline;
            this.flights = flights;
        }

        public Node(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "state = '" + state + '\''  +
                    ", came from = " + parent +
                    ", with = " + airline +
                    ", possible destinations = " + flights +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node)) return false;
            Node node = (Node) o;
            return state.equals(node.state) && Objects.equals(parent, node.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, parent);
        }

        static public List<String> solutionPath(Node somenode){
            List<String> statesxairline = new ArrayList<>();
            //going up the search tree to find the path
            while (somenode.parent!=null){
                statesxairline.add(somenode.state);
                statesxairline.add(somenode.airline);
                somenode = somenode.parent;
            }
            return statesxairline;
        }

    }
    //creating a function that generates a list of route records
    public static List<List<String>> routegenerator(String start, HashMap<String, List<String>> routes){
        return routes.keySet().parallelStream()
                .filter(key -> key.contains(start+"_"))
                .map(routes::get)
                .collect(Collectors.toList());
    }
    //creating a functions that generates the right route
    static <T> List<List<T>> separate(List<T> path, final int size) {
        List<List<T>> separated = new ArrayList<>();
        List<T> temp;
        for (int i = 0; i < path.size(); i += size - 1) {
            temp = (List<T>) path.subList(i, Math.min(path.size(), i + size));
            if (temp.size() != 1) {
                separated.add(new ArrayList<T>(temp));
            }
        }
        return separated;
    }    // first step is to turn user file path into a list with start and destination
    public static List<String> filePathToQuery(String filepath){
        // take user input
        List<String> query = new ArrayList<>();
        List<String> startstop = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            String line;
            while ((line = br.readLine()) != null) {
                String values = Arrays.toString(line.split(","));
                query.add(values);
            }
            // trimming the variables to get exactly what I need
            // trimming start city and country
            String startcountry = query.get(0).split(",")[1];
            startcountry = startcountry.substring(1,startcountry.length()-1);
            String startcity = query.get(0).split(",")[0];
            startcity = startcity.substring(1);

            //trimming for destination country and city
            String destcountry = query.get(1).split(",")[1];
            destcountry = destcountry.substring(1,destcountry.length()-1);
            String destcity = query.get(1).split(",")[0];
            destcity = destcity.substring(1);

            //appending them to my return variable
            startstop.add(startcity);
            startstop.add(startcountry);
            startstop.add(destcity);
            startstop.add(destcountry);

    } catch (ArrayIndexOutOfBoundsException | IOException e)
    {
    e.printStackTrace();
}
        return startstop;
    }
    // second step is to find routes with nodes containing starts and destinations
    public static List<String> pathFinder(List<String> startstop) {
        // save the file paths of information sources in strings to be read for later
        String airlinepath = "/Users/charliesgoodies/Desktop/cockpitfiles/airlines.csv";
        String airportpath = "/Users/charliesgoodies/Desktop/cockpitfiles/airports.csv";
        String routepath = "/Users/charliesgoodies/Desktop/cockpitfiles/routes.csv";

        // use line to loop through a file of choice
        String line = "";

        // save all the csv files into maps, using airline id as a key for both routes and airlines, and city for airport
        HashMap<String,List<String>> airlineIDs = new HashMap<>();
        HashMap<String, List<String>> airports = new HashMap<>();
        HashMap<String, List<String>> routes = new HashMap<>();

        String startcity = null;
        String stopcity = null;
        List<String> flightpath = new ArrayList<>();
        try {
            BufferedReader brAL = new BufferedReader(new FileReader(airlinepath));
            BufferedReader brAP = new BufferedReader(new FileReader(airportpath));
            BufferedReader brR = new BufferedReader(new FileReader(routepath));

            // using the city as a key for airports
            while ((line = brAP.readLine()) != null) {
                List<String> anairport = Arrays.asList(line.split(","));
                airports.put(anairport.get(2), anairport);
            }
            // using the airline ID as a key, we create a dictionary
            while ((line = brAL.readLine()) != null) {
                List<String> anairline = Arrays.asList(line.split(","));
                    List<String> idAndActivity = new ArrayList<>();
                    idAndActivity.add(anairline.get(1));
                    idAndActivity.add(anairline.get(7));
                    airlineIDs.put(anairline.get(0),idAndActivity);

            }
            // using the start and stop airport code as a key for routes
            while ((line = brR.readLine()) != null) {
                List<String> aroute = Arrays.asList(line.split(","));
                String startpart = aroute.get(2).concat("_");
                routes.put(startpart.concat(aroute.get(4)), aroute);
            }

            //testing access
//            System.out.println(airlines);
//            System.out.println(routes.get("LZH_KMG"));
//            System.out.println(airports.get("Tofino"));

            startcity = startstop.get(0);
            stopcity = startstop.get(2);

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // creating a search algorithm to find optimal path
            List<String> startAirportData = airports.get(startcity);
            String startAirportCode = startAirportData.get(4);

            List<String> stopAirportData = airports.get(stopcity);
            String stopAirportCode = stopAirportData.get(4);

            flightpath.add(startAirportCode);
            Node node = new Node(startAirportCode);
            List<Node> frontier = new ArrayList<>();
            List<Node> explored = new ArrayList<>();
            int stops = 0;


            if (startcity == stopcity) {
                flightpath.add(startAirportCode);
            }
            frontier.add(node);
            boolean sol = false;

            while (!sol) {
                node = frontier.get(0);
                frontier.remove(0);
                explored.add(node);
                System.out.println("popped: " + node.state);
                stops++;
                List<List<String>> allRoutesfromnode = routegenerator(node.state, routes);
                for (List<String> strings : allRoutesfromnode) {
                    Node child = null;
                    String child_start = strings.get(2);
                    String child_airline = strings.get(1);
                    child = new Node(strings.get(4), node, child_airline, routegenerator(child_start, routes));
                    if (airlineIDs.get(child_airline) != null) {
                        if ("Y".equals(airlineIDs.get(child_airline).get(1).trim())) {
                            if (!explored.contains(child) && !frontier.contains(child)) {
                                if (stopAirportCode.equals(child.state)) {
                                    flightpath = Node.solutionPath(child);
                                    sol = true;
                                }
                                frontier.add(child);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < flightpath.size(); i++) {
                if (i % 2!=0){
                    String airlinecode = airlineIDs.get(flightpath.get(i)).get(0);
                    flightpath.set(i,airlinecode);
                    }
            }
        }
        System.out.println(flightpath);
        return flightpath;
    }
    //third step is to print my answer from step 2 to a file
    public static void pathToTxt(List<String> startstop, String OG_filename){
        PrintWriter pw=null;
        List<List<String>> flights = separate(startstop, 3);
        List<List<String>> flights1 = new ArrayList<>();

        List<String> last = flights.get(flights.size()-1);
        List<String> lastreplace = new ArrayList<>();
        lastreplace.add(last.get(0));
        lastreplace.add(last.get(1));
        lastreplace.add("Your destination");

        for (int i = 0; i < flights.size()-2; i++) {
            flights1.add(flights.get(i));
        }
        flights1.add(lastreplace);

        try{
        pw = new PrintWriter(OG_filename+"_output");

            for (int i = 0; i < flights1.size(); i++) {
                pw.print((i+1)+". ");
                pw.print(flights1.get(i).get(1)+" ");
                pw.print("from "+flights1.get(i).get(0)+" ");
                pw.println("to "+flights1.get(i).get(2));

            }
            pw.println("Total flights: "+startstop.size()/3);

        pw.close();
            System.out.println("Printing to file complete");
    }
        catch(FileNotFoundException e){
        System.out.println(e.toString());
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Welcome to the C0CKPIT. enter your filepath to continue: ");
        Scanner scan = new Scanner(System.in);
        String input = scan.nextLine();
//        String tester = "/Users/charliesgoodies/Desktop/testingtesting.txt";
        List<String>holder = Arrays.asList(input.split("/"));
        String filename1 = holder.get(holder.size()-1);
        String filename = filename1.substring(0,filename1.length()-4);
        System.out.println("Using "+filename+" for search");

        System.out.print(filePathToQuery(input).get(0));
        System.out.println(" is your start city");
        List<String> useroutput;
        useroutput = pathFinder(filePathToQuery(input));
//        List<String> dummy = new ArrayList<>();
//        dummy.add("BA from ACC to LHR 0 stops");
//        dummy.add("DL from LHR to MSP 0 stops");
        pathToTxt(useroutput,filename);



    }
}
