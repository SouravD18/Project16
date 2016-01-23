package player;

public class Constants {
    //  Preflop constants
    public static double preFlopGreat = .6;
    public static double preFlopGood = .55;
    public static double preFlopAverage = .4;
    
    public static double preFlop3BetStandard = .85;
    public static double preFlop3betVariance = .15;
    public static double pfrPercent = .85;
    public static double pfrVariance = .15;
    
    // Flop constants
    public static double flopGreat = .8;
    public static double flopGood = .6;
    public static double flopAverage = .4; 
    
    // Turn constants
    public static double turnGreat = .8;
    public static double turnGood = .6;
    public static double turnAverage = .5;
    
    // River constants
    public static double riverGreat = .85;
    public static double riverGood = .65;
    public static double riverAverage = .55;
    
    // Post Flop constants
    public static double standardAggrFactor = 3;
    public static double aggrVariance = 3;
    public static double equityBarAggr = .1;
    
    public static double standardWentSD = .4;
    public static double wentSDVariance = .4;
    public static double equityBarWentSD = .1;
    
    public static double standardWinSD = .5;
    public static double winSDVariance = .3;
    public static double equityBarWinSD = .1;
    
    public static double adjustEquity = .1;
    
}
