package com.piepoint.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.piepoint.app.data.model.CartItem
import com.piepoint.app.ui.components.*
import com.piepoint.app.ui.theme.*
import com.piepoint.app.ui.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (String) -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by remember { derivedStateOf { cartViewModel.subtotal } }
    val deliveryFee by remember { derivedStateOf { cartViewModel.deliveryFee } }
    val total by remember { derivedStateOf { cartViewModel.total } }

    var name by remember { mutableStateOf("Alex Johnson") }
    var email by remember { mutableStateOf("alex.johnson@email.com") }
    var phone by remember { mutableStateOf("+1 (555) 234-5678") }
    var address by remember { mutableStateOf("123 Main Street, New York, NY 10001") }
    var paymentMethod by remember { mutableStateOf(0) } // 0=Card, 1=Cash

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Checkout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Order items summary
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Order Summary")
                Spacer(modifier = Modifier.height(12.dp))
                cartItems.forEach { item ->
                    CheckoutItemRow(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Divider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = Color(0xFFEEEEEE))

            // Delivery info
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Delivery Information")
                Spacer(modifier = Modifier.height(12.dp))
                CheckoutTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                Spacer(modifier = Modifier.height(10.dp))
                CheckoutTextField(value = email, onValueChange = { email = it }, label = "Email")
                Spacer(modifier = Modifier.height(10.dp))
                CheckoutTextField(value = phone, onValueChange = { phone = it }, label = "Phone")
                Spacer(modifier = Modifier.height(10.dp))
                CheckoutTextField(
                    value = address, onValueChange = { address = it },
                    label = "Delivery Address", minLines = 2
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Payment Method")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentMethodChip(
                        label = "\uD83D\uDCB3 Credit Card",
                        isSelected = paymentMethod == 0,
                        onClick = { paymentMethod = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    PaymentMethodChip(
                        label = "\uD83D\uDCB5 Cash",
                        isSelected = paymentMethod == 1,
                        onClick = { paymentMethod = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Price breakdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3EE))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    PriceSummaryRow("Subtotal", "$${String.format("%.2f", subtotal)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    PriceSummaryRow("Delivery Fee", "$${String.format("%.2f", deliveryFee)}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = OrangeAccent.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))
                    PriceSummaryRow(
                        "Total", "$${String.format("%.2f", total)}",
                        isBold = true, valueColor = OrangeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            GradientButton(
                text = "Place Order",
                onClick = {
                    val order = cartViewModel.placeOrder(name, address)
                    onOrderPlaced(order.id)
                },
                enabled = name.isNotBlank() && address.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CheckoutItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBackground),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = item.pizza.imageRes),
                contentDescription = item.pizza.name,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.pizza.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.selectedSize.label} × ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        Text(
            text = "$${String.format("%.2f", item.totalPrice)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangeAccent,
            focusedLabelColor = OrangeAccent
        )
    )
}

@Composable
private fun PaymentMethodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) OrangeAccent else CardBackground)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PriceSummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (isBold) TextPrimary else TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = valueColor
        )
    }
}
