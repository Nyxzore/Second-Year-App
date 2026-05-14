<?php
$host = "localhost";
$port = "5432";
$dbname = "dgroup2689";
$user = "sgroup2689";
$password_db = "c434b13a28cd859c169a"; 

$username = $_POST['username'];
$hash = $_POST['hash']; 
$mode = $_POST['mode'] ?? "login";
$email = $_POST['email'];
$is_admin = $_POST['is_admin'];

$conn_string = "host=$host port=$port dbname=$dbname user=$user password=$password_db";
$dbconn = pg_connect($conn_string);

if (!$dbconn) {
    echo json_encode(["status" => "error", "message" => "Database connection failed"]);
    exit;
}
    
function login() {
    global $dbconn, $username, $hash;
    if (empty($username) || empty($hash)) {
        return ["status" => "failure", "message" => "Username/Password cannot be empty"];
    }
    
    $SQL_query = "SELECT password, userid FROM accounts WHERE username = $1";
    $result = pg_query_params($dbconn, $SQL_query, array($username));
    
    if ($row = pg_fetch_assoc($result)) {
        if (hash_equals($row['password'], $hash)) {
            return ["status" => "success", "message" => "Logged in!", "uuid" => $row['userid']];
        }
    }
    return ["status" => "failure", "message" => "Invalid username or password"];
}

function create_account() {
    global $dbconn, $username, $hash, $email, $is_admin;
    if (empty($username) || empty($hash)) {
        return ["status" => "failure", "message" => "Username/Password cannot be empty"];
    }
    // 1. Check if user exists
    $check_query = "SELECT username FROM accounts WHERE username = $1";
    $check_res = pg_query_params($dbconn, $check_query, array($username));
    if (pg_fetch_assoc($check_res)) {
        return ["status" => "failure", "message" => "Username already taken"];
    }

    // 2. Insert new user
    $SQL_query = "INSERT INTO accounts (username, password, email, admin) VALUES ($1, $2, $3, $4) RETURNING userid";
    $result = pg_query_params($dbconn, $SQL_query, array($username, $hash, $email, $is_admin));
    
    
    if ($result) {
        $row = pg_fetch_assoc($result);
        return ["status" => "success", "message" => "Account created!", "uuid" => $row['userid']];
    }
    return ["status" => "error", "message" => "Failed to create account: " . pg_last_error($dbconn)];
}

if ($mode === "login") {
    $response = login();
} else if ($mode === "create_account") {
    $response = create_account(); 
}

pg_close($dbconn);

header('Content-Type: application/json');
echo json_encode($response);
?>