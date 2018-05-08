
/*
 * 动态规划
 * 钢条切割问题
 * price代表各个长度钢条的价钱price[length-1]
 * */

public class DynamicProgramming {  
  
    public static void main(String[] args) {  
      int[] price = new int[]{1, 5, 8, 9, 10, 17, 17, 20, 24, 30};
      int n = 4;
      //递归调用
      System.out.println(DynamicProgramming.cut(price, n));
      
      //备忘录形式递归调用
      int[] r = new int[n + 1];
      	//初始化r的值
      for(int i = 0; i < n + 1; i++){
    	r[i] = -1;  
      }
      System.out.println(DynamicProgramming.cutMemo(price, n, r));
      
      //自底向上动态规划
      
      System.out.println(DynamicProgramming.buttom_up_cut(price, n));
    } 
    
    //递归调用
    public static int cut(int[] p, int n){
    	int q = Integer.MIN_VALUE;
    	if(n == 0){
    		return 0;
    	}
    	for(int i = 1; i <= n; i++){
    		q = Math.max(q, p[i-1] + cut(p, n-i));
    	}
    	return q;
    } 
    
    //备忘录形式, 即利用一个数组进行记录某长度钢条是否被计算过
    public static int cutMemo(int[] p, int n, int[] r){
    	//如果备忘录中有值,则退出
    	if(r[n] != -1){
    		return r[n];
    	}
    	
   		int q = Integer.MIN_VALUE;
   		if(n == 0){
   			return 0;
   		}else{
   			for(int i = 1; i <= n; i++){
   	    		q = Math.max(q, p[i-1] + cut(p, n-i));
   	    	}
   			//更新备忘录的值
    		r[n] = q;
    	}
    	
    	return q;
    }
    
    //自底向上动态规划
    public static int buttom_up_cut(int[] p, int n){
    	int[] r = new int[n + 1];
    	
    	//该层循环从1-n寻找子结构的最优解, 即长度为1最优、长度为2最优....
    	for(int i = 1; i <= n; i++){
    		int q = Integer.MIN_VALUE;
    		
    		for(int j = 1; j <= i; j++){
    			q = Math.max(q, p[j-1] + r[i-j]);
    		}
    		r[i] = q;
    	}
    	
    	return r[n];
    }
}  