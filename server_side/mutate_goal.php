<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$descrip = $_POST['description'] ?? null;
$title = $_POST['title'] ?? null;
$due_date = $_POST['due_date'] ?? null;
$mode = $_POST['mode'] ?? null; //true if edit mode, false if add mode
$goal_id = $_POST['goal_id'] ?? null;

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
 echo "Unable to open database \n";
 exit;
} 
if ($mode === "edit") {
    $SQL_query = "UPDATE goals SET title = $1, description = $2, due_date = $3 WHERE id = $4;";
    $params = array($title, $descrip, $due_date, (int)$goal_id);
}
else if ($mode === "add") {
    $SQL_query = "INSERT INTO goals (description, title, due_date, user_uuid) VALUES ($1, $2, $3, $4);";
    $params = array($descrip, $title, $due_date, $uuid);
}
else {
    $SQL_query = "DELETE FROM goals WHERE id = $1;";
    $params = array((int)$goal_id);
}

$result = pg_query_params($dbconn, $SQL_query, $params);

if (!$result) {
    echo json_encode(["status" => "error", "message" => pg_last_error($dbconn)]);
} else {
    echo json_encode(["status" => "success", "message" => "change succesful"]);
}

pg_close($dbconn);
?>