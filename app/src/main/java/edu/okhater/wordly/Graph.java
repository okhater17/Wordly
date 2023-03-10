package edu.okhater.wordly;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class Graph{
    //Node class
    public static class Node{
        String name;
        ArrayList<String> successors;
        //Used in finding the path
        Node prev = null;

        public Node(String n, ArrayList<String> s){
            name = n;
            successors = s;

        }
        //Static function for when creating the graph
        public static boolean check(String word1, String word2){
            int count = 0;
            for(int i = 0; i < word1.length(); i++){
                if(word1.charAt(i) != word2.charAt(i)){
                    count += 1;
                }
            }
            return count == 1;
        }
    }
    //Probably don't need the number of nodes; but thought it would be good to have
    protected int numNodes = 0;
    //Nodes storage
    protected  ArrayList<Node> g = new ArrayList<>();
    //Constructor takes in a BufferedReader that already has the file from the assets loaded up (check the main activity)
    public Graph(BufferedReader br) throws IOException {
        String line;
        while((line = br.readLine()) != null){
            //No special characters and no capitals
            line = line.replaceAll("[^A-Za-z0-9]", "");
            line = line.toLowerCase();
            if(line.length() == 4){
                this.add(line);
            }
        }
    }
    //Adds to the graph
    private void add(String word){
        ArrayList<String> successor = new ArrayList<>();
        for (Node node : g){
            if(Node.check(node.name, word)){
                node.successors.add(word);
                successor.add(node.name);
            }
        }
        g.add(new Node(word, successor));
        numNodes += 1;
    }
    //Finds a node of that name, useful in finding a path
    public Node find(String name){
        for(Node node: g){
            if(node.name.equals(name)){
                return node;
            }
        }
        return null;
    }
    private ArrayList<String> findSuccessors(String name){
        for(Node node: g){
            if(node.name.equals(name)){
                return node.successors;
            }
        }
        return null;
    }
    //Could probably be improved by adding heuristics
    //For when the user types in their own words
    public ArrayList<String> findPath(String word1, String word2){

            Queue<Node> q = new LinkedList<>();
            Node curr = this.find(word1);
            q.add(curr);
            Set<String> set = new HashSet<>();
            set.add(curr.name);
            while (!q.isEmpty()) {
                curr = q.remove();
                if (curr.name.equals(word2)) {
                    ArrayList<String> ans = new ArrayList<>();
                    while (curr != null) {
                        ans.add(curr.name);
                        curr = curr.prev;
                    }
                    Collections.reverse(ans);
                    return ans;
                }

                ArrayList<String> successor = this.findSuccessors(curr.name);
                for (String s : Objects.requireNonNull(successor)) {
                    if (!set.contains(s)) {
                        Node found = this.find(s);
                        set.add(s);
                        Objects.requireNonNull(found).prev = curr;
                        q.add(found);
                    }
                }
            }
            //Couldn't find path; possible. Not sure how to handle yet.
            return null;
        }


    //Random generation from one word to another
    //Just 4 random words
    //Not sure what to do if there are multiple answers, this only generates one path
    public ArrayList<String> findRandomWordsPath(){
        Random rand = new Random();
        Node curr = g.get(rand.nextInt(g.size()));
        //To avoid trivial problems where the solution could be achieved in less than 4 steps
       ArrayList<String> otherSuccessors = new ArrayList<>();
        ArrayList<String> ans = new ArrayList<>();
        for(int i = 0; i < 4; i++){
            ans.add(curr.name);
           otherSuccessors.add(curr.name);
            ArrayList<String> successor = this.findSuccessors(curr.name);
            successor.removeAll(otherSuccessors);
            //Stuck, go again
            if(successor.size() == 0){
                return findRandomWordsPath();
            }
            curr = this.find(successor.get(rand.nextInt(successor.size())));
            otherSuccessors.addAll(successor);
        }
        return ans;
    }
}
