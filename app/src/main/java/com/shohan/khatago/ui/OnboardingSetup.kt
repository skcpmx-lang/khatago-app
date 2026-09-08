package com.shohan.khatago.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shohan.khatago.R
import com.shohan.khatago.ui.theme.KhataBackground
import com.shohan.khatago.ui.theme.KhataPrimary
import com.shohan.khatago.ui.theme.KhataPrimaryContainer
import com.shohan.khatago.ui.theme.KhataTextSecondary
import kotlinx.coroutines.launch

private val onboardingPages = listOf(
    "KhataGo" to "All your finances, in one place.",
    "Stay on top of what you owe." to "Track shop credit, loans, EMIs and personal debt in one place.",
    "Know where your money goes." to "Track income, expenses, payments and financial trends.",
    "Private by design." to "Your financial records stay on your device."
)

@Composable
fun OnboardingScreen(onSkip: () -> Unit, onContinue: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Column(
        modifier = Modifier.fillMaxSize().background(KhataBackground).padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_khatago_mark),
                        contentDescription = "KhataGo mark",
                        modifier = Modifier.size(68.dp)
                    )
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)) { page ->
                        val item = onboardingPages[page]
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(item.first, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                            Text(item.second, style = MaterialTheme.typography.bodyLarge, color = KhataTextSecondary)
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(onboardingPages.size) { index ->
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(if (pagerState.currentPage == index) 28.dp else 8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (pagerState.currentPage == index) KhataPrimary else KhataPrimaryContainer)
                    )
                }
            }
            Button(
                onClick = {
                    if (isLastPage) onContinue()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isLastPage) "Get Started" else "Continue")
            }
            TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Skip") }
        }
    }
}

@Composable
fun SetupScreen(name: String, onNameChange: (String) -> Unit, onContinue: () -> Unit, error: String?) {
    Column(
        modifier = Modifier.fillMaxSize().background(KhataBackground).imePadding().padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Spacer(Modifier.height(12.dp))
            Text("What's your name?", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter your name") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )
            OutlinedTextField(
                value = "Bangladeshi Taka (৳)",
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Currency") },
                shape = RoundedCornerShape(18.dp)
            )
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
            Text("Continue")
        }
    }
}
