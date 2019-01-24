app.controller("searchController",function($scope,$location,searchService){
	$scope.search=function(){
		$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
		searchService.search($scope.searchMap).success(
				function(response){
					$scope.resultMap = response;
					buildPageLabel();
				});
	}
	
	$scope.searchMap={"keywords":"","category":"","brand":"","spec":{},"price":"","pageNo":1,"pageSize":20,"sort":"DESC","sortField":""}
	
	$scope.addSearchItem=function(key,value){
		if(key=="category"||key=="brand"||key=="price"){
			$scope.searchMap[key] =value;
		}else{
			$scope.searchMap.spec[key]=value;
		}
		$scope.search();
	}
	
	$scope.removeSearchItem=function(key){
		if(key=="category"||key=="brand"||key=="price"){
			$scope.searchMap[key] ="";
		}else{
			delete $scope.searchMap.spec[key];
		}	
		$scope.search();
	}
	
	buildPageLabel=function(){
		$scope.pageLabel=[];
		var maxPageNo = $scope.resultMap.totalPage;
		var firstPage=1;
		var lastPage= maxPageNo ;
		$scope.firstDot = false;
		$scope.lastDot = false;
		if(maxPageNo >5){
			if($scope.searchMap.pageNo <= 3){
				lastPage = 5;
			}else if($scope.searchMap.pageNo>=lastPage-2){
				firstPage = maxPageNo - 4 ;
			}else{
				firstPage = $scope.searchMap.pageNo - 2;
				lastPage = $scope.searchMap.pageNo + 2;
			}
			
			if(firstPage >1){
				$scope.firstDot = true;
			}
			
			if(lastPage < maxPageNo){
				$scope.lastDot = true;
			}
			
		}
		
		for(var i = firstPage;i <= lastPage;i++){
			$scope.pageLabel.push(i);
		}
	}
	
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo >$scope.resultMap.totalPage){
			return;
		}
		$scope.searchMap.pageNo = pageNo;
		$scope.search();
	}
	
	$scope.isTopPage = function(){
		if($scope.searchMap.pageNo == 1){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.isEndPage = function(){
		if($scope.searchMap.pageNo == $scope.resultMap.totalPage){
			return true;
		}else{
			return false;
		}
	}
		
	
	changeSort=function(){
		
		if($scope.searchMap.sort == "DESC"){
			$scope.searchMap.sort = "ASC"
		}else{
			$scope.searchMap.sort = "DESC"
		}		
	}
	
	$scope.sortSearch=function(sortField){		
		$scope.searchMap.sortField = sortField;
		if(sortField != "updatetime"){
			changeSort();		
		}
		$scope.search();
	}
	
	$scope.keywordsIsBrand = function(){
		for(var i = 0;i < $scope.resultMap.brandList.length ;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			} 
		}
		return false;
	}
	
	$scope.loadKeywords = function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();
	}
});













