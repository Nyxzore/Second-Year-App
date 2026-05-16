<?php
header('Content-Type: application/json');
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a";

$uuid = $_POST['uuid'] ?? null;
$friend_id = $_POST['friend_id'] ?? null;

$dbconn = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$password_db");

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}

$SQL = "UPDATE friendships
        SET status = 'active'
        WHERE user_id1 = $1 AND user_id2 = $2 AND status = 'pending'";

$result = pg_query_params($dbconn, $SQL, array($friend_id, $uuid));

if ($result && pg_affected_rows($result) > 0) {
    echo json_encode(["status" => "success", "message" => "Friend request accepted!"]);
} else {
    echo json_encode(["status" => "failure", "message" => "Could not find or accept request"]);
}

pg_close($dbconn);
?>