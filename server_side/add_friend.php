<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$friend_username = $_POST['friend_username'] ?? null;

$dbconn = pg_connect("host=$host port=$port dbname=$dbname user=$user password=$password_db");

if (!$dbconn) {
    echo "Database connection failed";
    exit;
}

$res = pg_query_params($dbconn, "select userid from accounts where username = $1", array($friend_username));
$row = pg_fetch_assoc($res);

if (!$row) {
    echo "User not found";
    exit;
}

$friend_id = $row['userid'];

if ($uuid == $friend_id) {
    echo "You cannot add yourself";
    exit;
}

// Check if relationship already exists in EITHER direction
$check_sql = "select status from friendships where (user_id1 = $1 and user_id2 = $2) or (user_id1 = $2 and user_id2 = $1)";
$check_res = pg_query_params($dbconn, $check_sql, array($uuid, $friend_id));

if ($check_res && pg_num_rows($check_res) > 0) {
    $existing = pg_fetch_assoc($check_res);
    if ($existing['status'] === 'active') {
        echo "Already friends with " . $friend_username;
    } else {
        echo "Friend request already exists";
    }
    exit;
}

$sql = "insert into friendships (user_id1, user_id2, status) values ($1, $2, 'pending')";
$result = pg_query_params($dbconn, $sql, array($uuid, $friend_id));

if ($result) {
    echo "Request sent to " . $friend_username;
} else {
    echo "Error sending request";
}
pg_close($dbconn);
?>