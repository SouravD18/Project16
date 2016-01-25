package player;

public class Constants {
    //  Preflop constants
    public static final double preFlopGreat = .6;
    public static final double preFlopGood = .55;
    public static final double preFlopAverage = .4;
    public static final double reducePreflop = 0;
    
    public static final double preFlop3BetStandard = .85;
    public static final double preFlop3betVariance = .15;
    public static final double pfrPercent = .85;
    public static final double pfrVariance = .15;
    
    // Flop constants
    public static final double flopGreat = .8;
    public static final double flopGood = .6;
    public static final double flopAverage = .4; 
    public static final double reduceFlop = 0;
    
    // Turn constants
    public static final double turnGreat = .8;
    public static final double turnGood = .6;
    public static final double turnAverage = .5;
    public static final double reduceTurn = 0;

    public static final double polar = .7;
    
    public static final double avgFirstAggr = .4;
    public static final double avgFolding = .4;
    public static final double avgCheckRaising = .3;
    
    public static final double aggrScale = .9;
    public static final double foldingScale = .9;
    public static final double checkRaiseScale = .9;
    
    
//    public static double betBalance = 1;
//    public static double aggression = .5;
//    public static double checkAggression = .5;
//    public static double folding = .5;
    // River constants
    public static final double riverGreat = .85;
    public static final double riverGood = .65;
    public static final double riverAverage = .55;
    public static final double reduceRiver = 0;
    
    // Post Flop constants
    public static final double standardAggrFactor = 3;
    public static final double aggrVariance = 3;
    public static final double equityBarAggr = 0;
    
    public static final double standardWentSD = .4;
    public static final double wentSDVariance = .4;
    public static final double equityBarWentSD = 0;
    
    public static final double standardWinSD = .5;
    public static final double winSDVariance = .3;
    public static final double equityBarWinSD = 0;
    
    public static final double adjustEquity = .35;
    public static final double predictFold = 0;
}
