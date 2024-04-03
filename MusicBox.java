import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javafx.stage.FileChooser;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class MusicBox extends JFrame implements Runnable, ActionListener, AdjustmentListener{
    
    JFrame frame;
    JToggleButton[][] grid;
    JPanel gridPanel, buttonPanel, tempoPanel;
    String[] clipNames;
    JScrollPane scrollPane;
    Thread timing;
    Clip[] clips;
    String[] instrumentNames;
    JMenuBar menuBar;
    JMenu instrumentMenu, fileMenu, addRemoveMenu;
    JMenuItem save, load, addColumn, addNColumns, removeColumn, removeNColumns;
    JButton playStop, clear;
    JScrollBar tempoBar;
    JLabel tempoLabel;
    JFileChooser fileChooser;
    boolean currentlyPlaying = false;

    int tempo = 200;
    int currentColumn = 0;
    int numColumns = 50;


    public static void main(String[] args) {
        MusicBox musicBox = new MusicBox();
    }

    public  MusicBox(){
        this.setSize(900, 600);
        this.setLayout(new BorderLayout());

        clipNames = new String[]{"C4", "B3", "ASharp3", "A3", "GSharp3", "G3", "FSharp3", "F3", "E3", "DSharp3", "D3", "CSharp3", "C3", "B2", "ASharp2", "A2", "GSharp2", "G2", "FSharp2", "F2", "E2", "DSharp2", "D2", "CSharp2", "C1", "B1", "ASharp1", "A1", "GSharp1", "G1", "FSharp1", "F1", "E1", "DSharp1", "D1", "CSharp1", "C1"};

        clips = new Clip[clipNames.length];
        instrumentNames = new String[6];
        instrumentNames[0] = "Bell";
        instrumentNames[1] = "Glockenspiel";
        instrumentNames[2] = "Marimba";
        instrumentNames[3] = "Oboe";
        instrumentNames[4] = "Oh_Ah";
        instrumentNames[5] = "Piano";

        String currentDirectory = System.getProperty("user.dir");
        fileChooser =  new JFileChooser(currentDirectory);

        menuBar = new JMenuBar();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout(2, 1));

        playStop = new JButton("Play");
        playStop.addActionListener(this);

        clear = new JButton("Clear");
        clear.addActionListener(this);

        instrumentMenu = new JMenu("Instruments");
        for(int i=0;i<instrumentNames.length;i++){
            JMenuItem j = new JMenuItem(instrumentNames[i]);
            j.getClientProperty(instrumentNames[i]);
            j.addActionListener(this);
            instrumentMenu.add(j);
        }

        fileMenu = new JMenu("File");
        save = new JMenuItem("Save");
        load = new JMenuItem("Load");
        save.addActionListener(this);
        load.addActionListener(this);
        fileMenu.add(save);
        fileMenu.add(load);

        addRemoveMenu = new JMenu("Add/Remove");
        addColumn = new JMenuItem("Add Column");
        addNColumns = new JMenuItem("Add N Columns");
        removeColumn = new JMenuItem("Remove Column");
        removeNColumns = new JMenuItem("Remove N Columns");
        addColumn.addActionListener(this);
        addNColumns.addActionListener(this);
        removeColumn.addActionListener(this);
        removeNColumns.addActionListener(this);
        addRemoveMenu.add(addColumn);
        addRemoveMenu.add(addNColumns);
        addRemoveMenu.add(removeColumn);
        addRemoveMenu.add(removeNColumns);

        tempoBar = new JScrollBar(Scrollbar.HORIZONTAL, 200, 0, 50, 350);
        tempoBar.addAdjustmentListener(this);
        tempo = tempoBar.getValue();
        tempoLabel = new JLabel("    Tempo: "+tempo+"  ");

        tempoPanel = new JPanel();
        tempoPanel.setLayout(new BorderLayout());
        tempoPanel.add(tempoLabel, BorderLayout.WEST);
        tempoPanel.add(tempoBar, BorderLayout.CENTER);

        menuBar.add(fileMenu);
        menuBar.add(instrumentMenu);
        menuBar.add(addRemoveMenu);
        menuBar.add(clear);
        buttonPanel.add(menuBar, BorderLayout.NORTH);
        buttonPanel.add(tempoPanel, BorderLayout.SOUTH);
        

        setGrid(37, numColumns);

        loadTones(instrumentNames[0]);

        this.add(buttonPanel, BorderLayout.NORTH);
        this.add(playStop, BorderLayout.SOUTH);


        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        timing = new Thread(this);
        timing.start();

    }

    public void setGrid(int numRows, int numCols){
        if(scrollPane != null){
            this.remove(scrollPane);
        }
        grid = new JToggleButton[numRows][numCols];
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(numRows, numCols));

        for(int r=0;r<numRows;r++){
            String str = clipNames[r].replaceAll("Sharp", "#");
            for(int c=0;c<numCols;c++){
                JToggleButton tb = new JToggleButton(str);
                tb.setForeground(Color.BLACK);
                tb.setPreferredSize(new Dimension(30, 30));
                tb.setMargin(new Insets(0, 0, 0, 0));
                grid[r][c] = tb;
                gridPanel.add(tb);
            }
        }
        scrollPane = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);
        this.revalidate();

    }

    public void loadTones(String s){
        String initialInstrument = s;
        try{
            for(int r=0;r<clipNames.length;r++){
                String temp="/Users/rithikapathuri/Downloads/Data Structures/Music Box Tones - Courtesy of Dr Neg/";
                String str = temp+initialInstrument+"/"+initialInstrument+" - "+clipNames[r]+".wav";
                //System.out.println(str);
                File file = new File(str);
                AudioInputStream audioInitial = AudioSystem.getAudioInputStream(file);
                clips[r] = AudioSystem.getClip();
                clips[r].open(audioInitial);

            }
        }
        catch(UnsupportedAudioFileException|IOException|LineUnavailableException e){
            
        }
    }

    public void saveSong(){
        FileNameExtensionFilter filter = new FileNameExtensionFilter("*.txt", ".txt");
        fileChooser.setFileFilter(filter);

        if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try{
                String str = file.getAbsolutePath();
                if(str.contains(".txt")){
                    str = str.substring(0, str.length()-4);
                }
                String currentSong = "";
                String[] noteNames =   {"c ", "b ", "a- ", "a ", "g- ", "g ", "f-", "f ", "e ", "d- ", "d ", "c- ",
                                        "c ", "b ", "a- ", "a ", "g- ", "g ", "f-", "f ", "e ", "d- ", "d ", "c- ",
                                        "c ", "b ", "a- ", "a ", "g- ", "g ", "f-", "f ", "e ", "d- ", "d ", "c- ",
                                        "c "};
                for(int r=0;r<grid.length;r++){
                    currentSong += noteNames[r];
                    for(int c=0;c<grid[0].length;c++){
                        if(grid[r][c].isSelected()){
                            currentSong += 'X';
                        }
                        else{
                            currentSong += '-';
                        }
                    }
                    currentSong += "\n";
                }
                BufferedWriter outputStream = new BufferedWriter(new FileWriter(str+".txt"));
                outputStream.write(currentSong.substring(0, currentSong.length() - 1));
                outputStream.close();
            }
            catch(IOException e){

            }
        }

    }

    public void loadFile(){
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            try{
                File loadFile = fileChooser.getSelectedFile();
                BufferedReader input = new BufferedReader(new FileReader(loadFile));
                String temp = input.readLine();
                String[] pieces = temp.split(" ");
                tempo = new Integer(pieces[0]);
                numColumns = new Integer(pieces[1]);
                Character[][] charArray = new Character[37][numColumns];
                int row = 0;
                while((temp=input.readLine())!=null){
                    for(int c=2;c<(numColumns+2);c++){
                        charArray[row][c-2] = temp.charAt(c);
                    }
                    row++;
                }
                setNotes(charArray);
            }
            catch(IOException e){

            }
        }
    }

    public void setNotes(Character[][] a){
        scrollPane.remove(gridPanel);
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(37, numColumns));
        grid = new JToggleButton[37][numColumns];

        for(int r=0;r<grid.length;r++){
            String str = clipNames[r].replaceAll("Sharp", "#");
            for(int c=0;c<grid[0].length;c++){
                JToggleButton tb = new JToggleButton(str);
                tb.setForeground(Color.BLACK);
                tb.setPreferredSize(new Dimension(30, 30));
                tb.setMargin(new Insets(0, 0, 0, 0));
                grid[r][c] = tb;
                gridPanel.add(tb);
            }
        }
        this.remove(scrollPane);
        scrollPane = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);

        for(int r=0;r<a.length;r++){
            for(int c=0;c<a[0].length;c++){
                if(a[r][c] == 'X'){
                    grid[r][c].setSelected(true);
                }
            }
        }
        this.revalidate();

    }

    public void addColumn(int n){
        this.remove(scrollPane);
        scrollPane.remove(gridPanel);
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(37, numColumns+n));
        JToggleButton[][] temp = new JToggleButton[37][numColumns+n];

        for(int r=0;r<temp.length;r++){
            String str = clipNames[r].replaceAll("Sharp", "#");
            for(int c=0;c<temp[0].length;c++){
                JToggleButton tb = new JToggleButton(str);
                tb.setForeground(Color.BLACK);
                tb.setPreferredSize(new Dimension(30, 30));
                tb.setMargin(new Insets(0, 0, 0, 0));
                if(c<grid[r].length){
                    if(grid[r][c].isSelected()){
                        tb.setSelected(true);
                    }
                }
                temp[r][c] = tb;
                gridPanel.add(tb);
            }
        }
        grid = temp;
        scrollPane = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);
        this.revalidate();
    }

    public void removeColumn(int n){
        this.remove(scrollPane);
        scrollPane.remove(gridPanel);
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(37, numColumns - n));
        JToggleButton[][] temp = new JToggleButton[37][numColumns-n];

        for(int r=0;r<temp.length;r++){
            String str = clipNames[r].replaceAll("Sharp", "#");
            for(int c=0;c<temp[0].length;c++){
                if(c<grid[r].length){
                    JToggleButton tb = new JToggleButton(str);
                    if(grid[r][c].isSelected()){
                        tb.setSelected(true);
                    }
                    tb.setForeground(Color.BLACK);
                    tb.setPreferredSize(new Dimension(30, 30));
                    tb.setMargin(new Insets(0, 0, 0, 0));
                    grid[r][c] = tb;
                    gridPanel.add(tb);
                }
            }
        }
        grid = temp;
        scrollPane = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);
        this.revalidate();
    }

    public void reset(){
        currentColumn = 0;
        currentlyPlaying = false;
        playStop.setText("Play");
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent arg0) {
        if(arg0.getSource() == tempoBar){
            tempo = tempoBar.getValue();
            tempoLabel.setText("    Tempo: "+tempo+"  ");

        }
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if(arg0.getSource() == playStop){
            if(currentlyPlaying){
                currentlyPlaying = false;
                playStop.setText("Play");
            }
            else{
                currentlyPlaying = true;
                playStop.setText("Stop");
            }
        }
        else if(arg0.getSource() == clear){
            for(int r=0;r<grid.length;r++){
                for(int c=0;c<grid[0].length;c++){
                    grid[r][c].setSelected(false);
                }
            }
            reset();
        }
        else if(arg0.getSource() == save){
            reset();
            saveSong();
        }
        else if(arg0.getSource() == load){
            reset();
            loadFile();
        }
        else if(arg0.getSource() == addColumn){
            addColumn(1);
        }
        else if(arg0.getSource() == addNColumns){
            String str = JOptionPane.showInputDialog("Number of Columns to Add: ");
            int n = new Integer(str);
            if(n > 0){
                addColumn(n);
            }
        }
        else if(arg0.getSource() == removeColumn){
            if(numColumns > 2){
                removeColumn(1);
            }
        }
        else if(arg0.getSource() == removeNColumns){
            String str = JOptionPane.showInputDialog("Number of Columns to Remove: ");
            int n = new Integer(str);
            if(numColumns > n+1 && n > 0){
                removeColumn(n);
            }
        }
        else{
            String s =((JMenuItem)arg0.getSource()).getText();
            loadTones(s);
        }
        

    }

    @Override
    public void run() {
        while(true){
            try{
                if(!currentlyPlaying){
                    timing.sleep(0);
                }
                else{
                    for(int r=0;r<grid.length;r++){
                        if(grid[r][currentColumn].isSelected()){
                            clips[r].start();
                        }
                    }
                    timing.sleep(550 - tempo);
                    for(int r=0;r<grid.length;r++){
                        if(grid[r][currentColumn].isSelected()){
                            clips[r].stop();
                            clips[r].setFramePosition(0);
                        }
                    }
                    currentColumn++;
                    if(currentColumn == grid[0].length){
                        currentColumn = 0;
                    }
                }
            }catch(InterruptedException e){

            }
        }

    }
}
