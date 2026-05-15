<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a"; 

$uuid = $_POST['uuid'];

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password_db";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$q1 = "SELECT COUNT(id) as count FROM goals WHERE user_uuid = $1 AND completed = true";
$res1 = pg_query_params($dbconn, $q1, array($uuid));
$completed = pg_fetch_assoc($res1)['count'] ?? 0;

$q2 = "SELECT COUNT(id) as count FROM goals WHERE user_uuid = $1 AND completed = false";
$res2 = pg_query_params($dbconn, $q2, array($uuid));
$active = pg_fetch_assoc($res2)['count'] ?? 0;

$response = [
    "status" => "success",
    "completed_count" => (int)$completed,
    "active_count" => (int)$active
];

pg_close($dbconn);

header('Content-Type: application/json');
echo json_encode($response);
?>
