package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Player
import com.example.model.PositionRole
import com.example.ui.theme.CourtDark
import com.example.ui.theme.CourtLines
import com.example.ui.theme.CourtSurface
import com.example.ui.theme.VolleyballGold

@Composable
fun EditPlayerDialog(
    player: Player?,
    teamName: String,
    teamColorHex: Long,
    onDismiss: () -> Unit,
    onSavePlayer: (Player) -> Unit
) {
    var numberText by remember { mutableStateOf(player?.number?.toString() ?: "") }
    var nameText by remember { mutableStateOf(player?.name ?: "") }
    var selectedRole by remember { mutableStateOf(player?.role ?: PositionRole.OUTSIDE_HITTER) }
    var isCaptain by remember { mutableStateOf(player?.isCaptain ?: false) }
    var isLibero by remember { mutableStateOf(player?.isLibero ?: false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CourtSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CourtLines),
            modifier = Modifier.fillMaxWidth().testTag("edit_player_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(teamColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (player != null) "Edit Line-up Player" else "Add Line-up Player",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = teamName,
                                fontSize = 11.sp,
                                color = VolleyballGold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shirt Number Field
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 2) {
                            numberText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Shirt / Jersey Number (1-99)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_player_number_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VolleyballGold,
                        unfocusedBorderColor = CourtLines,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Player Name Field
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        errorMessage = null
                    },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_player_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VolleyballGold,
                        unfocusedBorderColor = CourtLines,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Role selection chips
                Text("Position / Role:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PositionRole.entries.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) VolleyballGold else Color(0xFF1E293B))
                                .clickable {
                                    selectedRole = role
                                    if (role == PositionRole.LIBERO) {
                                        isLibero = true
                                    } else if (isLibero) {
                                        isLibero = false
                                    }
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = role.code,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CourtDark else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Captain / Libero checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isCaptain = !isCaptain }
                    ) {
                        Checkbox(
                            checked = isCaptain,
                            onCheckedChange = { isCaptain = it },
                            colors = CheckboxDefaults.colors(checkedColor = VolleyballGold)
                        )
                        Text("Team Captain (C)", fontSize = 12.sp, color = Color.White)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLibero = !isLibero
                            if (isLibero) selectedRole = PositionRole.LIBERO
                            else if (selectedRole == PositionRole.LIBERO) selectedRole = PositionRole.OUTSIDE_HITTER
                        }
                    ) {
                        Checkbox(
                            checked = isLibero,
                            onCheckedChange = {
                                isLibero = it
                                if (it) selectedRole = PositionRole.LIBERO
                                else if (selectedRole == PositionRole.LIBERO) selectedRole = PositionRole.OUTSIDE_HITTER
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFA855F7))
                        )
                        Text("Libero [L]", fontSize = 12.sp, color = Color(0xFFD8B4FE))
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(err, color = Color(0xFFEF4444), fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val num = numberText.toIntOrNull()
                            if (num == null || num <= 0 || num > 99) {
                                errorMessage = "Please enter a valid shirt number (1-99)."
                                return@Button
                            }
                            if (nameText.isBlank()) {
                                errorMessage = "Please enter a player name."
                                return@Button
                            }

                            val updated = (player ?: Player(
                                number = num,
                                name = nameText.trim(),
                                role = selectedRole,
                                isCaptain = isCaptain,
                                isLibero = isLibero
                            )).copy(
                                number = num,
                                name = nameText.trim(),
                                role = selectedRole,
                                isCaptain = isCaptain,
                                isLibero = isLibero
                            )
                            onSavePlayer(updated)
                        },
                        modifier = Modifier.weight(1f).testTag("save_player_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VolleyballGold,
                            contentColor = CourtDark
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
