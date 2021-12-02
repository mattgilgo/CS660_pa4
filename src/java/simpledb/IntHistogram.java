package simpledb;

/** A class to represent a fixed-width histogram over a single integer-based field.
 */
public class IntHistogram {
    private int[] buckets;
    private int min;
    private int max;
    private int valueCount;
    private int bucketCount;
    private int modulo;


    /**
     * Create a new IntHistogram.
     * 
     * This IntHistogram should maintain a histogram of integer values that it receives.
     * It should split the histogram into "buckets" buckets.
     * 
     * The values that are being histogrammed will be provided one-at-a-time through the "addValue()" function.
     * 
     * Your implementation should use space and have execution time that are both
     * constant with respect to the number of values being histogrammed.  For example, you shouldn't 
     * simply store every value that you see in a sorted list.
     * 
     * @param buckets The number of buckets to split the input value into.
     * @param min The minimum integer value that will ever be passed to this class for histogramming
     * @param max The maximum integer value that will ever be passed to this class for histogramming
     */
    public IntHistogram(int buckets, int min, int max) {
    	// some code goes here
        
        this.bucketCount = buckets;
        this.buckets = new int[buckets];
        this.min = min;
        this.max = max;
        this.valueCount = 0;
        this.modulo = (int)Math.ceil((double)(max-min+1)/buckets);
    }

    /**
     * Add a value to the set of values that you are keeping a histogram of.
     * @param v Value to add to the histogram
     */
    public void addValue(int v) {
    	// some code goes here

        int properBucket = (v-this.min)/this.modulo;
        //int arrLen = this.buckets.length;
        this.buckets[properBucket]++;   // may be out of index
        this.valueCount++;
    }

    private double estimateSelectivityWhenEquals(int v) {
        int properBucket = (v - this.min)/this.modulo;
        if (properBucket < 0) {
            properBucket = -1;
        } else if (properBucket >= this.bucketCount) {
            properBucket = this.bucketCount;
        }

        if (properBucket < 0) {
            return 0.0;
        } else if (properBucket >= this.bucketCount) {
            return 0.0;
        }
        int h = this.buckets[properBucket];
        double dub = (double)((double)h/this.modulo)/this.valueCount;
        return dub;
    }

    private double estimateSelectivityWhenNotEquals(int v, Predicate.Op op) {
        int properBucket = (v - this.min)/this.modulo;
        if (properBucket < 0) {
            properBucket = -1;
        } else if (properBucket >= this.bucketCount) {
            properBucket = this.bucketCount;
        }

        int leftBucket = 0;
        int rightBucket = 0;
        int hereBucket = 0;
        int h = 0;
        double sel = 0.0;

        if (properBucket >= this.bucketCount) {
            leftBucket = this.bucketCount-1;
            rightBucket = this.bucketCount;
            hereBucket = 0;
            h = 0;
        } else if (properBucket < 0) {
            leftBucket = -1;
            rightBucket = 0;
            hereBucket = 0;
            h = 0;
        } else {
            leftBucket = properBucket-1;
            rightBucket = properBucket+1;
            hereBucket = -1;
            h = this.buckets[properBucket];
        }

        switch(op) {
            case LESS_THAN:
                if (hereBucket == -1) {
                    hereBucket = (v - (this.modulo*leftBucket) + this.min)/this.modulo;
                }
                sel = h * hereBucket / this.valueCount;
                if (leftBucket < 0) {
                    double dub = sel / this.valueCount;
                    return dub;
                }
                for (int i = leftBucket; i >= 0; i--) {
                    sel += this.buckets[i];
                }
                double dub2 = sel / this.valueCount;
                return dub2;
            case GREATER_THAN:
                if (hereBucket == -1) {
                    hereBucket = ((this.modulo*rightBucket) + this.min - v)/this.modulo;
                }
                sel = h * hereBucket / this.valueCount;
                if (rightBucket > this.bucketCount) {
                    double dub = sel / this.valueCount;
                    return dub;
                }
                for (int j = rightBucket; j < this.bucketCount; j++) {
                    sel += this.buckets[j];
                }
                double dub3 = sel / this.valueCount;
                return dub3;
        default:
            return -1.0;
        }
    }

    /**
     * Estimate the selectivity of a particular predicate and operand on this table.
     * 
     * For example, if "op" is "GREATER_THAN" and "v" is 5, 
     * return your estimate of the fraction of elements that are greater than 5.
     * 
     * @param op Operator
     * @param v Value
     * @return Predicted selectivity of this particular operator and value
     */
    public double estimateSelectivity(Predicate.Op op, int v) {

    	// some code goes here

        switch(op) {
            case EQUALS:
                return estimateSelectivityWhenEquals(v);
            case NOT_EQUALS:
                return 1.0 - estimateSelectivityWhenEquals(v);
            case LIKE:
                return estimateSelectivityWhenEquals(v);
            case GREATER_THAN:
                return estimateSelectivityWhenNotEquals(v, op);
            case LESS_THAN:
                return estimateSelectivityWhenNotEquals(v, op);
            case GREATER_THAN_OR_EQ:
                return (estimateSelectivityWhenNotEquals(v, Predicate.Op.GREATER_THAN) + estimateSelectivityWhenEquals(v));
            case LESS_THAN_OR_EQ:
                return (estimateSelectivityWhenNotEquals(v, Predicate.Op.LESS_THAN) + estimateSelectivityWhenEquals(v));
            default:
                return -1.0;
        }
    }
    
    /**
     * @return
     *     the average selectivity of this histogram.
     *     
     *     This is not an indispensable method to implement the basic
     *     join optimization. It may be needed if you want to
     *     implement a more efficient optimization
     * */
    public double avgSelectivity()
    {
        // some code goes here
        return 1.0;
    }
    
    /**
     * @return A string describing this histogram, for debugging purposes
     */
    public String toString() {
        // some code goes here

        String returnStr = "";

        for (int i = 0; i < this.bucketCount; i++) {
            returnStr += "Bucket Number" + i + ":";

            for (int j = 0; j < this.buckets[i]; j++) {
                returnStr += " , ";
            }

            returnStr += "\n";
        }

        return returnStr;
        
    }
}
