package edu.okhater.wordly;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
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
        String line = "";
        while((line = br.readLine()) != null){
            if(line.length() == 4){
                this.add(line);
            }
        }
    }
    //Adds to the graph
    private void add(String word){
        ArrayList<String> succ = new ArrayList<>();
        for (Node node : g){
            if(Node.check(node.name, word)){
                node.successors.add(word);
                succ.add(node.name);
            }
        }
        g.add(new Node(word, succ));
        numNodes += 1;
    }
    //Finds a node of that name, useful in finding a path
    private Node find(String name){
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
    public ArrayList<String> findPath(String word1, String word2){
        Queue<Node> q = new LinkedList<>();
        Node curr = this.find(word1);
        q.add(curr);
        Set<String> set = new HashSet<>();
        while(!q.isEmpty()){
            curr = q.remove();
            if(curr.name.equals(word2)){
                ArrayList<String> ans = new ArrayList<>();
                while(curr != null){
                    ans.add(curr.name);
                    curr = curr.prev;
                }
                Collections.reverse(ans);
                return ans;
            }
            set.add(curr.name);
            ArrayList<String> successor = this.findSuccessors(curr.name);
            for(String s: successor){
                if(!set.contains(s)) {
                    Node found = this.find(s);
                    found.prev = curr;
                    q.add(found);
                }
            }
        }
        //Couldn't find path; possible. Not sure how to handle yet.
        return null;
    }
    //Random generation from one word to another
    public ArrayList<String> findRandomWordsPath(){
        return null;
    }
}
