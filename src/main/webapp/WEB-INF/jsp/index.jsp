<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>CI 변환 시스템</title>


	<script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.17.1/xlsx.full.min.js"></script>
	<script src="https://uicdn.toast.com/grid/latest/tui-grid.js"></script>
	<link rel="stylesheet" href="https://uicdn.toast.com/grid/latest/tui-grid.css" />


	<!-- 공통 -->
	<link rel="stylesheet" href="/resource/css/style.css" />
	<!-- Drag&Drop -->
	<link rel="stylesheet" href="/resource/css/file-drag-and-drop.css" />
	<script defer src="/resource/js/file-drag-and-drop.js"></script>


	<script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>

<script type="text/javascript">
window.onload = function(){
	document.getElementById('fExcel').addEventListener('change', excelExport);
	document.querySelector('#findBtn').addEventListener('click', findData);
	stateDragAndDrop.callback = excelExport;	//File Drag&Drop callback 이벤트 초기화


	/* ============================================== */
	/* ==== 로딩바 ==== */
	$( document ).ajaxStart(function() {
		// $('html').css("cursor", "wait");	//마우스 커서를 로딩 중 커서로 변경
		$('#loading').show();
	});
	$( document ).ajaxStop(function() {
		// $('html').css("cursor", "auto");	//마우스 커서를 원래대로 돌린다
		$('#loading').hide();
	});
	/* ============================================== */
}


let excelExport = (event)=>{
	state.init();
	excelExportCommon(event, handleExcelDataAll);
}
function excelExportCommon(event, callback){
	document.querySelector('#lbFile').innerHTML = event.target.files[0].name;

    let input = event.target;
    let reader = new FileReader();
    reader.onload = function(){
        let fileData = reader.result;
        let wb = XLSX.read(fileData, {type : 'binary'});
        let sheetNameList = wb.SheetNames; // 시트 이름 목록 가져오기
        sheetNameList.forEach(function(sheetName){
	        let sheet = wb.Sheets[sheetName]; // 첫번째 시트 
	        callback(sheet, sheetName);      
        });
        state.feature();	//기능함수 호출
    	instance.resetData(state.resultInfo[0].data); //데이터출력
        
    };
    reader.readAsBinaryString(input.files[0]);
}
function handleExcelDataAll(sheet, sheetName){
	handleExcelDataHeader(sheet); // header 정보 
	handleExcelDataJson(sheet); // json 형태
	handleExcelDataCsv(sheet); // csv 형태
	handleExcelDataHtml(sheet); // html 형태
	handleExcelDataGrid(sheet, sheetName); // grid 형태
}
function handleExcelDataHeader(sheet){
    let headers = get_header_row(sheet);
    $("#displayHeaders").html(JSON.stringify(headers));
}
function handleExcelDataJson(sheet){
    $("#displayExcelJson").html(JSON.stringify(XLSX.utils.sheet_to_json (sheet)));
}
function handleExcelDataCsv(sheet){
    $("#displayExcelCsv").html(XLSX.utils.sheet_to_csv (sheet));
}
function handleExcelDataHtml(sheet){
    $("#displayExcelHtml").html(XLSX.utils.sheet_to_html (sheet));
}

let state = {
	init: function(){
		this.currentSheetIdx=0;
		this.resultInfo = [];
	},
	currentSheetIdx : 0,
	defColumn: [
		//첫번째 시트 컬럼 정의..
		{
			name: '성명',
			jid: '주민번호'
		}
	],
	resultInfo: [],
	feature: function(){
		let sheet1 = this.resultInfo[0].data;

	}
}
function handleExcelDataGrid(sheet, sheetName){
	let mSheet = {
		sheetName: sheetName,
		title: '',
		col: state.defColumn[state.currentSheetIdx],
		data: []
	};

	//HeaderName..
	let headers = get_header_row(sheet);
	mSheet.title = getHeaderName(headers);
	
	//Header..
	const dataset = XLSX.utils.sheet_to_json(sheet);
	for(let col in dataset[0]){
		for(key in mSheet.col){
			if(mSheet.col[key]==dataset[0][col]){	//컬럼명칭이 일치하면...
				mSheet.col[key]=col;	//col 값으로 replace
				break;
			}
		}
	}
	
	
	//Body..
	for(let i=0; i<dataset.length; i++){
		let row = dataset[i];
		let data = {};
		for(let key in mSheet.col){
			data[key] = row[mSheet.col[key]];
		}		
		mSheet.data.push(data);
	}
	
	//Push Dataset..
	state.resultInfo.push(mSheet); 
	
	function getHeaderName(headers){
		headers.forEach(function(text, idx){
			headers[idx] = text.replace(/UNKNOWN+ [0-9]/gi,'');
		});
		return headers.join('').trim();
	}
	
}
// 출처 : https://github.com/SheetJS/js-xlsx/issues/214
function get_header_row(sheet) {
    let headers = [];
    let range = XLSX.utils.decode_range(sheet['!ref']);
    let C, R = range.s.r; /* start in the first row */
    /* walk every column in the range */
    for(C = range.s.c; C <= range.e.c; ++C) {
        let cell = sheet[XLSX.utils.encode_cell({c:C, r:R})] /* find the cell in the first row */

        let hdr = "UNKNOWN " + C; // <-- replace with your desired default 
        if(cell && cell.t) hdr = XLSX.utils.format_cell(cell);

        headers.push(hdr);
    }
    return headers;
}


let findData = ()=>{
	if(instance.getData() == null || instance.getData() == '' || instance.getData().length == 0){
		alert('파일을 첨부하세요.');
		return false;
	}


	const jsonParam = JSON.stringify(
			instance.getData()
					.map(row => {
						let obj = {};
						obj.name = row.name;
						obj.jid = row.jid;
						return obj;
					})
	);

	$.ajax({
		type : "POST",
		url : "/nice/ci",
		dataType: 'json',
		contentType: 'application/json; charset=utf-8',
		data : jsonParam,
		success : function(resp){
			let data = instance.getData()
						.map(row => {
							row.ci = getMatchedCi(resp.resultInfo, row.jid)
							return row;
						});

			instance.resetData(data);

		},
		error : function(XMLHttpRequest, textStatus, errorThrown){ // 비동기 통신이 실패할경우 error 콜백으로 들어옵니다.
			alert("통신 실패.")
		}
	});

	function getMatchedCi(mJidInfo, jid){
		let jidInfo = mJidInfo[jid];

		if(jidInfo.errCode == 'OK')
			return jidInfo['resultInfo'];
		else
			return jidInfo['errMsg'];
	}
}

</script>

<style type="text/css">
	div.content{
		margin: 10px;
	}
/*	button {
		height:27px;
		padding: 10px 15px 30px 15px;
		margin: 10px;
		background: #4f4f4f;
		color: aliceblue;
		border-radius:3px;
		font-size:14px;
		font-weight: bold;
		display: inline-block;
		width: auto;
		border-color: #6a5a64;
		text-shadow: none;
		box-shadow: none;
	}*/

</style>

</head>
<body>
<%--	<div id="loading" style="display: none">--%>
<%--		<div id="loading-bar">--%>
<%--			<img src="/resource/css/images/loading.gif">--%>
<%--			<p style="font-size: x-large; font-width: bold;">변환 중 입니다...</p>--%>
<%--		</div>--%>
<%--	</div>--%>



	<div class="app-container">
		<div class="app-item nav">
			<jsp:include page="nav.jsp"></jsp:include>
		</div>

		<div class="app-item article">
			<div class="box_group">
				<div class="app-container" style="align-items: center; display: block;">
					<h1>CI 변환</h1>
					<details>
						<summary>메뉴 세부정보</summary>
						<ul class="box">
							<li>주민번호에 대한 CI 값을 취득 합니다.</li>
							<li>첨부파일은 엑셀문서(xls,xlsx)만 가능 합니다.</li>
							<li>첨부파일의 시트에는 `A열:성명, B:주민번호` 가 작성되어 있어야 합니다.</li>
							<span style="font-weight: bold;">[첨부파일 작성방법]</span>
							<ul>
								<li>1행: 컬럼명(A열:성명, B열:주민번호)</li>
								<li>2~xxx행: 데이터</li>
							</ul>
						</ul>
					</details>
				</div>
				<div class="content">
				<span>
					<input type="button" value="파일 첨부하기"
						   onclick="document.querySelector('#lbFile').click(); return false;"/>
					<label id="lbFile" for="fExcel">선택된 파일이 없습니다</label>
				</span>
					<input type="file" id="fExcel" name="fExcel" style="display: none;"/>
					<div class="file-drag-and-drop">
						<p>첨부파일(xls,xlsx)을 이곳에 올려주세요</p>
					</div>
					<!-- 		<h1>Header 정보 보기</h1> -->
					<!-- 		<div id="displayHeaders"></div> -->
					<!-- 		<h1>JSON 형태로 보기</h1> -->
					<!-- 		<div id="displayExcelJson"></div> -->
					<!-- 		<h1>CSV 형태로 보기</h1> -->
					<!-- 		<div id="displayExcelCsv"></div> -->
					<!-- 		<h1>HTML 형태로 보기</h1> -->
					<!-- 		<div id="displayExcelHtml"></div> -->
				</div>
				<div>
					<button id="findBtn">
						<span>CI 변환 실행</span>
					</button>
				</div>
			</div>
			<%--box_group 종료--%>

			<div id="grid" class="tuigrid">

				<div id="loading" style="display: none" class="inbox-screen">
					<div id="loading-bar">
						<img src="/resource/css/images/loading.gif">
						<p style="font-size: x-large; font-width: bold;">변환 중 입니다...</p>
					</div>
				</div>

			</div>
		</div>
	</div>
</body>


<script type="text/javascript">
const options = {
	includeHiddensColumns: true,
	onlySelected: true,
	delimiter: '|',
	fileName: '주민번호 CI 변환',
}
let exportExcel = (format) => {
	instance.export(format, options);
}



//import Grid from 'tui-grid'; /* ES6 */
const Grid = tui.Grid;

const instance = new Grid({
	el: document.getElementById('grid'), // Container element
	contextMenu: ({ rowKey, columnName }) => (
			// 2차원 배열을 반환
			[
				[
					{
						name: 'expt',
						label: '내보내기',
						subMenu: [
							{
								name: 'subExpt1',
								label: 'Excel',
								action: () => {
									exportExcel('xlsx');
								}
							},
							{
								name: 'subExpt2',
								label: 'CSV',
								action: () => {
									exportExcel('csv');
								}
							},
						],
					},
				]
			]
	),

	// data: {
	// 	initialRequest: false,
	// 	api: {
	// 		readData: { url: 'nice/ci', method: 'POST'},
	// 		modifyData: { url: 'nice/ci', method: 'POST'}
	// 	},
	// 	contentType: 'application/json',
	// 	serializer(params){
	// 		return JSON.stringify(instance.getData()
	// 				.map(row => {
	// 					let obj = {};
	// 					obj.name = row.name;
	// 					obj.jid = row.jid;
	// 					return obj;
	// 				}));
	// 	}
	// 	LoadingBar: false
	// },
	rowHeaders: ['rowNum'],
	bodyHeight: 450,
	draggable: true,
	columnOptions: {
		resizable: true
	},
	columns: [
		{
			header: '성명',
			name: 'name',
			align: 'center',
			editor: 'text',
			width: 150,
			filter: 'select',
			sortingType: 'desc',
			sortable: true
		},
		{
			header: '주민번호',
			name: 'jid',
			align: 'center',
			editor: 'text',
			width: 150,
			filter: 'select',
			sortingType: 'desc',
			sortable: true,
		},
		{
			header: 'CI',
			name: 'ci',
			minWidth: 300,
			filter: 'select',
			sortingType: 'desc',
			sortable: true
		}
	]
});

// instance.resetData(newData); // Call API of instance's public method

Grid.applyTheme('striped'); // Call API of static method

</script>

</html>