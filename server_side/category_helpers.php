<?php
function parse_category_ids($str) {
    if (!$str) return [];
    $parts = explode(',', $str);
    $ids = [];
    foreach ($parts as $p) {
        $p = trim($p);
        if ($p != "") $ids[] = $p;
    }
    return $ids;
}

function sync_goal_categories($db, $gid, $cats) {
    pg_query_params($db, "delete from goal_categories where goal_id = $1", array($gid));
    foreach ($cats as $cid) {
        pg_query_params($db, "insert into goal_categories (goal_id, category_id) values ($1, $2)", array($gid, $cid));
    }
}

function sync_habit_categories($db, $hid, $cats) {
    pg_query_params($db, "delete from habit_categories where habit_id = $1", array($hid));
    foreach ($cats as $cid) {
        pg_query_params($db, "insert into habit_categories (habit_id, category_id) values ($1, $2)", array($hid, $cid));
    }
}

function fetch_categories_for_goals($db, $gids) {
    $map = [];
    if (!$gids) return $map;
    $placeholders = [];
    for ($i = 1; $i <= count($gids); $i++) {
        $placeholders[] = '$' . $i;
    }
    $sql = "select gc.goal_id, c.id, c.name from goal_categories gc join categories c on c.id = gc.category_id where gc.goal_id in (" . implode(',', $placeholders) . ") order by c.name asc";
    $res = pg_query_params($db, $sql, $gids);
    while ($row = pg_fetch_assoc($res)) {
        $id = $row['goal_id'];
        if (!isset($map[$id])) $map[$id] = [];
        $map[$id][] = array("id" => $row['id'], "name" => $row['name']);
    }
    return $map;
}

function fetch_categories_for_habits($db, $hids) {
    $map = [];
    if (!$hids) return $map;
    $placeholders = [];
    for ($i = 1; $i <= count($hids); $i++) {
        $placeholders[] = '$' . $i;
    }
    $sql = "select hc.habit_id, c.id, c.name from habit_categories hc join categories c on c.id = hc.category_id where hc.habit_id in (" . implode(',', $placeholders) . ") order by c.name asc";
    $res = pg_query_params($db, $sql, $hids);
    while ($row = pg_fetch_assoc($res)) {
        $id = $row['habit_id'];
        if (!isset($map[$id])) $map[$id] = [];
        $map[$id][] = array("id" => $row['id'], "name" => $row['name']);
    }
    return $map;
}

function fetch_all_categories($db, $uid) {
    $sql = "select id, name from categories where user_id = $1 order by name asc";
    $res = pg_query_params($db, $sql, array($uid));
    $categories = [];
    while ($row = pg_fetch_assoc($res)) {
        $categories[] = array("id" => $row['id'], "name" => $row['name']);
    }
    return $categories;
}
?>
