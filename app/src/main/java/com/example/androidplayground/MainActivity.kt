package com.example.androidplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidplayground.ui.theme.AndroidPlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidPlaygroundTheme {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(buttonsConfig) { buttonConfig ->
                        MenuButton(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(60.dp),
                            text = buttonConfig.text,
                            textColor = buttonConfig.textColor,
                            buttonColor = buttonConfig.buttonColor,
                        )
                    }
                }
            }
        }
    }
}

data class MenuButtonOptions(
    val id:Int,
    val text: String,
    val textColor : Color,
    val buttonColor: Color,
)

val buttonsConfig : List<MenuButtonOptions> = listOf(
    MenuButtonOptions(1,"Explore Features",Color.Black, Color.White),
    MenuButtonOptions(2,"Anroid Docs",Color.White, Color(0xFF3CDA84)),
)

@Composable
fun MenuButton(modifier: Modifier,text:String, buttonColor: Color, textColor: Color){
    Button(
        modifier = Modifier.border(
            color = Color.Black,
            width = 0.2.dp,
            shape = RoundedCornerShape(50)
        ),
        onClick = { println("[ $text ] Button Clicked") },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    )
    {
        Text(text= text, color = textColor)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidPlaygroundTheme {
//        Greeting("Android")
    }
}