package move;

public class GenerateMagicBitBoards {

    public static void makeBitboardDatabase() {
        generateBishopFancyMagicTable();
        generateRookFancyMagicTable();
    }
    public static void generateBishopFancyMagicTable() {
        for (int square=0;square<64;square++) {
            MagicBitboards.bishopAttacks[square] = new long[1<<(64-MagicBitboards.bishopBitshiftBy[square])];

            long bishopMask = getMaskForBishop(square);
            int maxBlockerCombinations = 1<<Long.bitCount(bishopMask);

            for (int blockerCombo=0;blockerCombo<maxBlockerCombinations;blockerCombo++) {
                long bishopBlockers = getBlockerCombo(blockerCombo,bishopMask);
                long attackDBIndex = MagicBitboards.bishopMagics[square]*bishopBlockers >>> MagicBitboards.bishopBitshiftBy[square];

                MagicBitboards.bishopAttacks[square][(int)attackDBIndex]=PieceAttack.generateBishopAttackBitboard(square,bishopBlockers);
            }
        }
    }
    public static void generateRookFancyMagicTable() {
        for (int square=0;square<64;square++) {
            MagicBitboards.rookAttacks[square] = new long[1<<(64-MagicBitboards.rookBitshiftBy[square])];

            long rookMask = getMaskForRook(square);
            int maxBlockerCombinations = 1<<Long.bitCount(rookMask);

            for (int blockerCombo=0;blockerCombo<maxBlockerCombinations;blockerCombo++) {
                long rookBlockers = getBlockerCombo(blockerCombo,rookMask);
                long attackDBIndex = MagicBitboards.rookMagics[square]*rookBlockers >>> MagicBitboards.rookBitshiftBy[square];

                MagicBitboards.rookAttacks[square][(int)attackDBIndex]=PieceAttack.generateRookAttackBitboard(square,rookBlockers);
            }
        }
    }

    //used this to find all the magic numbers and bitShifts inside the magicBitboard class
    public static void findFancyMagicBitboardForBishop(int bishopSquare) {
        for (int i=9;i>1;i--) {
            findMagicBitboardForBishop(bishopSquare,i);
            System.out.println("with bits remaining of "+i);
        }
    }
    public static void findFancyMagicBitboardForRook(int rookSquare) {
        for (int i=12;i>1;i--) {
            findMagicBitboardForRook(rookSquare,i);
            System.out.println("with bits remaining of "+i);
        }
    }

    //for finding magic bitboards, I have them saved in another class, so I don't have to generate them at runtime
    public static void findMagicBitboardForBishop(int bishopSquare, int bitsRemaining) {

        long bishopMask = getMaskForBishop(bishopSquare);
        int maxBlockerCombinations = 1<<Long.bitCount(bishopMask);

        boolean magicBitboardDoesntWork = true;
        while (magicBitboardDoesntWork) {
            long[] bishopAttacksArray = new long[512];
            int blockerComboNumber=0;
            long potentialMagicBitboard = getLargeRandomNumber() & getLargeRandomNumber() & getLargeRandomNumber();

            for (int i=0;i<maxBlockerCombinations;i++) {
                long bishopBlockers = getBlockerCombo(i,bishopMask);
                int attackDBIndex = (int)(potentialMagicBitboard*bishopBlockers >>> 64-bitsRemaining);
                long bishopAttacks = PieceAttack.generateBishopAttackBitboard(bishopSquare,bishopBlockers);

                if (bishopAttacksArray[attackDBIndex]!=0 && bishopAttacksArray[attackDBIndex]!=bishopAttacks) {//if there is a bad collision
                    break;
                }

                bishopAttacksArray[attackDBIndex]= bishopAttacks;
                blockerComboNumber++;
            }
            if (blockerComboNumber==maxBlockerCombinations){//bitboard works with all combinations of blockers
                magicBitboardDoesntWork=false;
                System.out.println("0x"+Long.toHexString(potentialMagicBitboard)+"L,");
            }
        }
    }
    public static void findMagicBitboardForRook(int rookSquare, int bitsRemaining) {
        long rookMask = getMaskForRook(rookSquare);
        int maxBlockerCombinations = 1<<Long.bitCount(rookMask);

        boolean magicBitboardDoesntWork = true;
        while (magicBitboardDoesntWork) {
            long[] rookAttacksArray = new long[4096];
            int blockerComboNumber=0;
            long potentialMagicBitboard = getLargeRandomNumber() & getLargeRandomNumber() & getLargeRandomNumber();

            for (int i=0;i<maxBlockerCombinations;i++) {
                long rookBlockers = getBlockerCombo(i,rookMask);
                int attackDBIndex = (int)(potentialMagicBitboard * rookBlockers >>> 64-bitsRemaining);
                long rookAttacks = PieceAttack.generateRookAttackBitboard(rookSquare,rookBlockers);
                
                if (rookAttacksArray[attackDBIndex]!=0 && rookAttacksArray[attackDBIndex]!=rookAttacks) {//if there is a bad collision
                    break;
                }
                rookAttacksArray[attackDBIndex]= rookAttacks;
                blockerComboNumber++;

            }
            if (blockerComboNumber==maxBlockerCombinations){//bitboard works with all combinations of blockers
                magicBitboardDoesntWork=false;
                System.out.println("0x"+Long.toHexString(potentialMagicBitboard)+"L,");
            }
        }
    }



//start with blockerNumber 0, not 1
    public static long getBlockerCombo(int blockerNumber, long mask) {//0s in the blockerNum are bits I leave alone
        long ret=mask;
        int bitMask;
        long tempMask=mask;
        for (int i=0;i<4096;i++) {
         bitMask = blockerNumber & 1<<i;
         int indexOfFirstBit= Long.numberOfTrailingZeros(tempMask);

         if (bitMask !=0) {
             ret^=1L<<indexOfFirstBit;
         }

         blockerNumber-=bitMask;
         tempMask^=1L<<indexOfFirstBit;
         if (blockerNumber==0)break;
        }

        return ret;
    }

    public static long getLargeRandomNumber() {
        long num1 = (long)(Math.random()*100000);
        long num2 = (long)(Math.random()*100000)<<16;
        long num3 = (long)(Math.random()*100000)<<32;
        long num4 = (long)(Math.random()*100000)<<48;
        return num1 + num2 + num3 + num4;
    }

    public static long[] maskForBishop = new long[64];
    public static long[] maskForRook = new long[64];
    public static long getMaskForBishop(int startingSquare) {
        return maskForBishop[startingSquare];
    }
    public static long getMaskForRook(int startingSquare) {
        return maskForRook[startingSquare];
    }

}
