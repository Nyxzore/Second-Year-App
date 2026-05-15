<?php

function parse_category_ids($raw) {
    if ($raw === null || $raw === '') {
        return [];
    }
    $parts = explode(',', $raw);
    $ids = [];
    foreach ($parts as $part) {
        $part = trim($part);
        if ($part !== '' && ctype_digit($part)) {
            $ids[] = (int)$part;
        }
    }
    return array_values(array_unique($ids));
}

function sync_goal_categories($dbconn, $goal_id, $category_ids, $user_uuid) {
    pg_query_params($dbconn, "DELETE FROM goal_categories WHERE goal_id = $1", array((int)$goal_id));

    foreach ($category_ids as $category_id) {
        pg_query_params(
            $dbconn,
            "INSERT INTO goal_categories (goal_id, category_id) VALUES ($1, $2)",
            array((int)$goal_id, (int)$category_id)
        );
    }
}

function sync_habit_categories($dbconn, $habit_id, $category_ids, $user_uuid) {
    pg_query_params($dbconn, "DELETE FROM habit_categories WHERE habit_id = $1", array((int)$habit_id));

    foreach ($category_ids as $category_id) {
        pg_query_params(
            $dbconn,
            "INSERT INTO habit_categories (habit_id, category_id) VALUES ($1, $2)",
            array((int)$habit_id, (int)$category_id)
        );
    }
}

function fetch_categories_for_goals($dbconn, $goal_ids) {
    $map = [];
    if (empty($goal_ids)) {
        return $map;
    }

    $placeholders = [];
    $params = [];
    $i = 1;
    foreach ($goal_ids as $gid) {
        $placeholders[] = '$' . $i;
        $params[] = (int)$gid;
        $i++;
    }

    $sql = "
        SELECT gc.goal_id, c.id, c.name
        FROM goal_categories gc
        JOIN categories c ON c.id = gc.category_id
        WHERE gc.goal_id IN (" . implode(',', $placeholders) . ")
        ORDER BY c.name ASC
    ";
    $result = pg_query_params($dbconn, $sql, $params);
    if (!$result) {
        return $map;
    }

    while ($row = pg_fetch_assoc($result)) {
        $goal_id = $row['goal_id'];
        if (!isset($map[$goal_id])) {
            $map[$goal_id] = [];
        }
        $map[$goal_id][] = ["id" => $row['id'], "name" => $row['name']];
    }
    return $map;
}

function fetch_categories_for_habits($dbconn, $habit_ids) {
    $map = [];
    if (empty($habit_ids)) {
        return $map;
    }

    $placeholders = [];
    $params = [];
    $i = 1;
    foreach ($habit_ids as $hid) {
        $placeholders[] = '$' . $i;
        $params[] = (int)$hid;
        $i++;
    }

    $sql = "
        SELECT hc.habit_id, c.id, c.name
        FROM habit_categories hc
        JOIN categories c ON c.id = hc.category_id
        WHERE hc.habit_id IN (" . implode(',', $placeholders) . ")
        ORDER BY c.name ASC
    ";
    $result = pg_query_params($dbconn, $sql, $params);
    if (!$result) {
        return $map;
    }

    while ($row = pg_fetch_assoc($result)) {
        $habit_id = $row['habit_id'];
        if (!isset($map[$habit_id])) {
            $map[$habit_id] = [];
        }
        $map[$habit_id][] = ["id" => $row['id'], "name" => $row['name']];
    }
    return $map;
}
