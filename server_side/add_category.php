<?php
header('Content-Type: application/json');

$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password = "c434b13a28cd859c169a";
$uuid = $_POST['uuid'] ?? null;
$category_name = trim($_POST['category_name'] ?? '');

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Unable to open database"]);
    exit;
}

if (empty($uuid) || $category_name === '') {
    echo json_encode(["status" => "error", "message" => "Missing uuid or category name"]);
    exit;
}

if (strlen($category_name) > 50) {
    echo json_encode(["status" => "error", "message" => "Category name too long (max 50)"]);
    exit;
}

$SQL_query = "INSERT INTO categories (name, user_uuid) VALUES ($1, $2) RETURNING id, name";
$result = pg_query_params($dbconn, $SQL_query, array($category_name, $uuid));

if (!$result) {
    $err = pg_last_error($dbconn);
    if (strpos($err, 'unique') !== false) {
        echo json_encode(["status" => "error", "message" => "Category already exists"]);
    } else {
        echo json_encode(["status" => "error", "message" => $err]);
    }
    exit;
}

$row = pg_fetch_assoc($result);
pg_close($dbconn);

echo json_encode([
    "status" => "success",
    "message" => "Category added",
    "category" => $row
]);
