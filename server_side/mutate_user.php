<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a"; 

$mode = $_POST['mode'];
$uuid = $_POST['uuid'];
$profile_picture_index = $_POST['profile_pic'];

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password_db";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}
    
function update_profile_pic() {
    global $dbconn, $username, $hash;
    if (empty($username) || empty($hash)) {
        return ["status" => "failure", "message" => "Username/Password cannot be empty"];
    }
    
    $SQL_query = "UPDATE accounts SET profile_picture = $1 WHERE userid = $2";
    $result = pg_query_params($dbconn, $SQL_query, array($profile_picture_index, $uuid));
    return ["status" => "success"];
}

if ($mode === "update_profile_pic") {
    $response = update_profile_pic();
    
pg_close($dbconn);

header('Content-Type: application/json');
echo json_encode($response);
?>