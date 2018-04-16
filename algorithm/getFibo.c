//use loop to get fibonaci number

int getResult(int n){
		int result = 0;
		int a = 1, b = 1, temp = 0;
		if(n == 1){
			return 1;
		}else if(n == 2){
			return 1;
		}else{
			for(int i = 2; i < n; i++){
				result = a + b;
				a = b;
				b = result;
			}
			return result;
		}
}