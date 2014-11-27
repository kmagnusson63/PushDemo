<?php
	/**

			Push Demo

	*/
	
	/*
		Change to your server settings
	*/
	define('DB_HOST','68.178.143.5');
	define('DB_USER','rrcproject');
	define('DB_PASS','UserPass1!');
	define('DB_NAME','rrcproject');
	define('LOG_FILE','push_demo.log');

	//	Server key from Google Developer Site for project
	define('GOOGLE_KEY','AIzaSyDVG_aZVNCHhemntGg-NP7YNGht_NLGyHQ');

	// Create a MySQLi resource object called $db.
	$db = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME); 

	// If an error occurs we can look here for more info:
	$connection_error = mysqli_connect_errno();
	$connection_error_message = mysqli_connect_error();
	
	/*
		Puts $string to Log File
	*/
	function logg($string)
	{
		$temp = "<" . db_time_now() . ">" . $string . "\n";
		$log = fopen(LOG_FILE, 'a');
		fwrite($log, $temp);
		fclose($log);
	}

	/*
		Returns server time
	*/
	function db_time_now()
	{
		return date("Y-m-d H:i:s");
	}

	/*
			Exports var_dump() method to a string variable for logging
	*/
	function varDumpToString ($var)
	{
	    ob_start();
	    var_dump($var);
	    return ob_get_clean();
	}

	/*
			Sends $message to $device
			$device = Device ID from Google Cloud Messaging
	*/
	function send_msg($device,$message){
		$data = array('registration_ids' => array($device),'data' => array( "message" => $message ));
		$data_string = json_encode($data);
		// logg("Sending:\n" . $data_string);
		$push_header = array("Content-Type: application/json","Authorization: key=" . GOOGLE_KEY);
		// logg("\nHeader:\n".varDumpToString($push_header));
		$crl = curl_init("https://android.googleapis.com/gcm/send");
		curl_setopt($crl, CURLOPT_HTTPHEADER, $push_header);
		curl_setopt($crl, CURLOPT_POST, true);
		curl_setopt ($crl, CURLOPT_SSL_VERIFYHOST, 0);
    	curl_setopt ($crl, CURLOPT_SSL_VERIFYPEER, 0);
		curl_setopt($crl, CURLOPT_POSTFIELDS, $data_string);
		curl_setopt($crl, CURLOPT_RETURNTRANSFER, true);
		$result = curl_exec($crl);
		curl_close($crl);
		logg("Sent to: " . $device);
		logg("RESULT: " . varDumpToString($result));
	}

	logg("Accessed by " . $_SERVER['REMOTE_ADDR']."\n");

	// Message to be sent from webpage entry
	if(isset($_GET['send_message'])){
		if(isset($_GET['message'])){
			$message = $_GET['message'];
		}
		else
		{
			$message = "Sent from webpage";
		}

		// Get last inserted registration id from the database
		$sql = 'SELECT `registration_id` FROM android_devices ORDER BY `id` DESC LIMIT 1' ;
		$result = $db->query($sql);
		$data = $result->fetch_assoc();

		// Add log entry and send message
		send_msg($data['registration_id'],$message);
		// logg(varDumpToString($data));
	}

	// Save registration id sent from device into databas
	if(isset($_POST['registration_id'])){
		$sql = 'INSERT INTO android_devices (registration_id) VALUES("'.$_POST['registration_id'].'")';
		$result = $db->query($sql);
		if($result){
			logg("Saved: ".$_POST['registration_id']);
		}
		else{
			logg("Failed saving: ".$_POST['registration_id']);
		}
	}
	// Send message sent from device
	elseif(isset($_POST['device'])){
		send_msg($_POST['device'],$_POST['message']);
	}
	// Received post but data not recognized, log data
	elseif(isset($_POST)){
		logg('DATA NOT RECOGNIZED FROM ' . $_SERVER['REMOTE_ADDR']);
		logg(varDumpToString($_POST));
	}

?>