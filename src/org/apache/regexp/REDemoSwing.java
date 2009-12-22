/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.regexp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * Interactive demonstration and testing harness for regular expressions classes.
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @version $Id$
 */
//public class REDemo extends Applet// implements TextListener
public class REDemoSwing extends JPanel implements ActionListener
{
    /**
     * Matcher and compiler objects
     */
    RE r = new RE();
    REDebugCompiler compiler = new REDebugCompiler();

    /**
     * Components
     */
    JTextField fieldRE;          // Field for entering regexps
    JTextField fieldMatch;       // Field for entering match strings
    JTextArea outRE;             // Output of RE compiler
    JTextArea outMatch;          // Results of matching operation

    /**
     * Add controls and init applet
     */
    public void init()
    {
        // Add components using the dreaded GridBagLayout
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        fieldRE = new JTextField("\\[([:javastart:][:javapart:]*)\\]", 80);
        gb.setConstraints(add(fieldRE), c);
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.anchor = GridBagConstraints.EAST;
        c.gridy = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        gb.setConstraints(add(fieldMatch = new JTextField("aaa([foo])aaa", 80)), c);
        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = GridBagConstraints.RELATIVE;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.weightx = 1.0;
        gb.setConstraints(add(outRE = new JTextArea()), c);
        c.gridy = 2;
        c.gridx = GridBagConstraints.RELATIVE;
        gb.setConstraints(add(outMatch = new JTextArea()), c);

        // Listen to text changes
        fieldRE.addActionListener(this);
        fieldMatch.addActionListener(this);

        // Initial UI update
        actionPerformed(null);
    }

    /**
     * Say something into RE text area
     * @param s What to say
     */
    void sayRE(String s)
    {
        outRE.setText(s);
    }

    /**
     * Say something into match text area
     * @param s What to say
     */
    void sayMatch(String s)
    {
        outMatch.setText(s);
    }

    /**
     * Convert throwable to string
     * @param t Throwable to convert to string
     */
    String throwableToString(Throwable t)
    {
        String s = t.getClass().getName();
        String m;
        if ((m = t.getMessage()) != null)
        {
            s += "\n" + m;
        }
        return s;
    }

    /**
     * Change regular expression
     * @param expr Expression to compile
     */
    void updateRE(String expr)
    {
        try
        {
            // Compile program
            r.setProgram(compiler.compile(expr));

            // Dump program into RE feedback area
            CharArrayWriter w = new CharArrayWriter();
            compiler.dumpProgram(new PrintWriter(w));
            sayRE(w.toString());
	    System.out.println(expr);
            System.out.println(w);
        }
        catch (Exception e)
        {
            r.setProgram(null);
            sayRE(throwableToString(e));
        }
        catch (Throwable t)
        {
            r.setProgram(null);
            sayRE(throwableToString(t));
        }
    }

    /**
     * Update matching info by matching the string against the current
     * compiled regular expression.
     * @param match String to match against
     */
    void updateMatch(String match)
    {
        try
        {
            // If the string matches the regexp
            if (r.match(match))
            {
                // Say that it matches
                String out = "Matches.\n\n";

                // Show contents of parenthesized subexpressions
                for (int i = 0; i < r.getParenCount(); i++)
                {
                    out += "$" + i + " = " + r.getParen(i) + "\n";
                }
                sayMatch(out);
            }
            else
            {
                // Didn't match!
                sayMatch("Does not match");
            }
        }
        catch (Throwable t)
        {
            sayMatch(throwableToString(t));
        }
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(e);
        // If it's a generic update or the regexp changed...
        if (e == null || e.getSource() == fieldRE)
        {
            // Update regexp
            updateRE(fieldRE.getText());
        }

        // We always need to update the match results
        updateMatch(fieldMatch.getText());
    }

    /**
     * Main application entrypoint.
     * @param arg Command line arguments
     */
    static public void main(String[] arg)
    {
        JFrame f = new JFrame("RE Demo");
        // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        REDemoSwing demo = new REDemoSwing();
        f.getContentPane().add(demo);
        demo.init();
        f.pack();
        f.setVisible(true);
    }
}
