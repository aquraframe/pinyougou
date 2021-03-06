 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,itemCatService,uploadService,goodsService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		var id = $location.search()['id'];
		if(id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.goodsDesc.introduction);
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				var list = $scope.entity.itemsList;
				for(var i = 0;i<list.length;i++){
					list[i].spec=JSON.parse(list[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){		
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
				function(response){
					if(response.success){
						//重新查询 
			        	alert("保存成功");//重新加载
//			        	$scope.entity={};
//			        	editor.html('');
			        	location.href="./goods.html"
					}else{
						alert(response.message);
					}
				}		
		);				
	}
	
	//添加
	$scope.add=function(){	
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					//重新查询 
		        	alert("保存成功");//重新加载
		        	$scope.entity={};
		        	editor.html('');
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(
				function(response){
					if(response.success){
						$scope.image_entity.url=response.message;						
					}else{
						alert(response.message);
					}
				}).error(function(){
					alert("上传文件发生错误")
				})
	}
    
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	$scope.selectItemCat1List=function(){
		itemCatService.findByParentId(0).success(
				function(response){
					$scope.itemCat1List=response;
				});
		
	}
	
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat2List=response;
				});
	});
	
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCat3List=response;
				});
	});
	
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		itemCatService.findOne(newValue).success(
				function(response){
					$scope.entity.goods.typeTemplateId=response.typeId;
				});
	});
	
	
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		typeTemplateService.findOne(newValue).success(
				function(response){
					$scope.typeTemplate=response;
					$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
					var id = $location.search()['id'];
					if(id == null){
						$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
					}
				});
		typeTemplateService.findSpecList(newValue).success(
				function(response){
					$scope.specList = response;
				});
	});
	
	$scope.updateSpecAttribute=function($event,name,value){
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		if(object!=null){
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	$scope.createItemList=function(){
		$scope.entity.itemsList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		var items= $scope.entity.goodsDesc.specificationItems;
		for(var i = 0; i<items.length;i++){
			$scope.entity.itemsList = addColumn($scope.entity.itemsList,items[i].attributeName,items[i].attributeValue);
		}
	}
	
	addColumn=function(list,columnName,columnValues){
		var newList=[];
		for(var i=0;i<list.length;i++){
			var oldRow=list[i];
			for(var j=0 ; j < columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName]=columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	$scope.status=["未审核","已审核","审核已通过","关闭"];
	
	$scope.itemCatList=[];
	
	$scope.findItemCatlist=function(){
		itemCatService.findAll().success(
				function(response){
					for(var i = 0;i < response.length;i++){
						$scope.itemCatList[response[i].id] = response[i].name;
					}
				});
	}
	
	$scope.checkAttributeValue=function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}
		if(object.attributeValue.indexOf(optionName)>=0){
			return true;
		}
		return false;
	}
	
	$scope.updateMarket=function(isMarketable){
		goodsService.updateMarket($scope.selectIds,isMarketable).success(
				function(response){					
						alert(response.message);				
				});
	}
	
});	
