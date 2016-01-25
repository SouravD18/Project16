package player;

public class Constants {
    //  Preflop constants
    public static double preFlopGreat = .6;
    public static double preFlopGood = .55;
    public static double preFlopAverage = .4;
    public static double reducePreflop = 0;
    
    public static double preFlop3BetStandard = .85;
    public static double preFlop3betVariance = .15;
    public static double pfrPercent = .85;
    public static double pfrVariance = .15;
    
    // Flop constants
    public static double flopGreat = .8;
    public static double flopGood = .6;
    public static double flopAverage = .4; 
    public static double reduceFlop = 0;
    
    // Turn constants
    public static double turnGreat = .8;
    public static double turnGood = .6;
    public static double turnAverage = .5;
    public static double reduceTurn = 0;

    public static double polar = .7;
    
    public static double avgFirstAggr = .4;
    public static double avgFolding = .4;
    public static double avgCheckRaising = .3;
    
    public static double aggrScale = .9;
    public static double foldingScale = .9;
    public static double checkRaiseScale = .9;
    
    
//    public static double betBalance = 1;
//    public static double aggression = .5;
//    public static double checkAggression = .5;
//    public static double folding = .5;
    // River constants
    public static double riverGreat = .85;
    public static double riverGood = .65;
    public static double riverAverage = .55;
    public static double reduceRiver = 0;
    
    // Post Flop constants
    public static double standardAggrFactor = 3;
    public static double aggrVariance = 3;
    public static double equityBarAggr = 0;
    
    public static double standardWentSD = .4;
    public static double wentSDVariance = .4;
    public static double equityBarWentSD = 0;
    
    public static double standardWinSD = .5;
    public static double winSDVariance = .3;
    public static double equityBarWinSD = 0;
    
    public static double adjustEquity = .35;
    public static double predictFold = 0;
    
    
}
