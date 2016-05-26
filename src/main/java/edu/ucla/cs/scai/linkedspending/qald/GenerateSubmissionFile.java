/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class GenerateSubmissionFile {

    public static void main(String[] args) throws Exception {
        String filePath = "/home/massimo/QALD-6trainingset.txt";
        String outputPath = "/home/massimo/QALD-6trainingset.json";
        String dataSetName = "qald-6-train-multilingual";

        JsonSubmission submission = new JsonSubmission(new JsonDataset(dataSetName));
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        while (true) {
            String l = in.readLine();
            while (l != null && l.length() == 0) {
                l = in.readLine();
            }
            if (l == null) {
                break;
            }
            System.out.println(l);
            String[] t = l.split("\\)");
            String id = t[0].trim();
            if (id.equals("8")) {
                System.out.print("");
            }
            l = l.replace(id + ") ", "");

            JsonUtterance utterance = new JsonUtterance("en", l);

            l = in.readLine();
            while (l.length() == 0) {
                l = in.readLine();
            }

            StringBuilder sb = new StringBuilder();
            while (l!=null && l.length() > 0) {
                sb.append(l).append(" ");
                l = in.readLine();
            }

            String sparql = sb.toString().trim().replaceAll("\\s+", " ");
            if (sparql.equals("OUT OF SCOPE")) {
                JsonQuestion question = new JsonQuestion(id, new JsonUtterance[]{utterance}, new JsonQuery(null), new JsonAnswer[0]);
                submission.questions.add(question);
            } else {

                l = in.readLine();
                while (l.length() == 0) {
                    l = in.readLine();
                }

                sb = new StringBuilder();
                while (l != null && l.length() > 0) {
                    sb.append(l).append(" ");
                    l = in.readLine();
                }

                String jsAnswer = sb.toString();
                JsonAnswer jsonAnswer = gson.fromJson(jsAnswer, JsonAnswer.class);

                JsonQuestion question = new JsonQuestion(id, new JsonUtterance[]{utterance}, new JsonQuery(sparql), new JsonAnswer[]{jsonAnswer});

                submission.questions.add(question);
            }
        }
        try (PrintWriter out = new PrintWriter(new FileOutputStream(outputPath, false), true)) {
            out.print(gson.toJson(submission));
        }
    }

}
