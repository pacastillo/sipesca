
var deviceInfo = function() {
	//saltar();
};


function init() {
    document.addEventListener("deviceready", deviceInfo, true);			
}


function saltar() {
    if( navigator.network.connection.type === "none" || navigator.network.connection.type === null ){
    	navigator.notification.alert(
		"No hay conexión disponible. Por favor, habilite la conexión a Internet para poder acceder a la información del servidor.", 
		afterAlert,
		"Aviso",
		"Ok"
		);
    	//navigator.app.exitApp();
    }else{
    	 window.location="http://sipesca.ugr.es";
    }
}


function confirmarsalida() { 
	navigator.notification.confirm(
	"¿Quieres salir?", 
	onConfirm,
	"Salir", 
	"Sí,No"
	);
}
function onConfirm(buttonIndex) { 
	if (buttonIndex==1){
		navigator.app.exitApp();
	}
	//else{
	//	alert("gracias por quedarte  :) ");
	//}
}
	
function acercade() { 
	navigator.notification.alert(
	"Aplicación móvil del proyecto SIPESCA para acceder a la información del servidor.", 
	afterAlert,
	"Acerca de...",
	"Ok"
	);
}
function afterAlert() {}


function contactWebServer(url) {
		xmlhttp=new XMLHttpRequest();
		xmlhttp.onreadystatechange=function() {
		if (xmlhttp.readyState==4 && (xmlhttp.status==200 || xmlhttp.status==0)) {
			var tmp = xmlhttp.responseText;
			document.getElementById("informacionObtenida").innerHTML = tmp;
		}
	}
	xmlhttp.open("GET",url,true);
	xmlhttp.send();
}
function getMyIP() {
	// en la func se devuelve el texto ya formateado y se muestra en la etiqueta abajo
	contactWebServer('http://sipesca.ugr.es/getinfo.php'); 
}

