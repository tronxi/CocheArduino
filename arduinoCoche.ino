#include <Ultrasonic.h>

const int izquierda = 10;//marron
const int izquierdaA = 8;//amarillo
const int izquierdaR = 9;//naranja

const int derecha = 5;//verde
const int derechaA = 7;//morado
const int derechaR = 6;//azul

const int numSensores = 6;

const int trig0 = 13;
const int echo0 = 12;

const int trig1 = 3;//naranja sensor
const int echo1= 2;//amarillo sensor

const int trig2 = 4;
const int echo2 = 11;

const int trig3 = 15;
const int echo3 = 14;

const int trig4 = 17;
const int echo4 = 16;

const int trig5 = 19;
const int echo5 = 18;

int velocidad;
char estado = 'p';
int valido;

Ultrasonic sensores[numSensores] = {Ultrasonic (trig0, echo0), Ultrasonic(trig1, echo1), Ultrasonic(trig2, echo2), 
                          Ultrasonic(trig3, echo3), Ultrasonic(trig4, echo4), Ultrasonic(trig5, echo5)};

int distancia[numSensores];

void setup()
{
  //pinMode(izquierda, OUTPUT);
  pinMode(izquierdaA, OUTPUT);
  pinMode(izquierdaR, OUTPUT);

  //pinMode(derecha, OUTPUT);
  pinMode(derechaA, OUTPUT);
  pinMode(derechaR, OUTPUT);
  Serial.begin(9600);
  velocidad = 255;
  valido = 1;
}

void loop()
{

  if (Serial.available())
  {
    char entrada = Serial.read();
    if (entrada == 'w')
    {
      avanzar(velocidad);
    }
    else if (entrada == 's')
    {
      retroceder(velocidad);
    }
    else if (entrada == 'a')
    {
      girarIzquierda();
    }
    else if (entrada == 'd')
    {
      girarDerecha();
    }
    else if (entrada == '+')
    {
      aumentarVelocidad();
    }
    else if (entrada == '-')
    {
      disminuirVelocidad();
    }
    else if(entrada == ' ')
    {
      pararMotor();
    }
    else if(entrada == 'm')
    {
      pararGiro();
    }
    else if(entrada == 'h')
    {
      autonomo();
    }
    else if(entrada == 'u')
    {
      seguir();
    }
    else if(entrada == 'p')
    {
      comprobarSensores();
    }
  }
}
void aumentarVelocidad()
{
  if(velocidad + 50 > 255)
  {
    velocidad = 255;
  }
  else
  {
    velocidad += 50;
  }
  if(estado == 'w')
  {
    avanzar(velocidad);
  }
  else if(estado == 's')
  {
    retroceder(velocidad);
  }
}
void disminuirVelocidad()
{
  if(velocidad - 50 <= 50)
  {
    velocidad = 50;
  }
  else
  {
    velocidad -= 50;
  }
  if(estado == 'w')
  {
    avanzar(velocidad);
  }
  else if(estado == 's')
  {
    retroceder(velocidad);
  }
}
void derechaAvanzar(int v)
{
  digitalWrite(derechaR, LOW);
  digitalWrite(derechaA, HIGH);
  analogWrite(derecha, v);
}
void  derechaRetroceder(int v)
{
  digitalWrite(derechaA, LOW);
  digitalWrite(derechaR, HIGH);
  analogWrite(derecha, v);
}
void derechaParar()
{
  digitalWrite(derechaR, LOW);
  digitalWrite(derechaA, LOW);
}
void izquierdaAvanzar(int v)
{
  digitalWrite(izquierdaR, LOW);
  digitalWrite(izquierdaA, HIGH);
  analogWrite(izquierda, v);
}
void izquierdaRetroceder(int v)
{
  digitalWrite(izquierdaA, LOW);
  digitalWrite(izquierdaR, HIGH);
  analogWrite(izquierda, v);
}
void izquierdaParar()
{
  digitalWrite(izquierdaR, LOW);
  digitalWrite(izquierdaA, LOW);
}

void avanzar(int v)
{
  estado = 'w';
  derechaAvanzar(v);
  izquierdaAvanzar(v);
}

void retroceder(int v)
{
  estado = 's';
  derechaRetroceder(v);
  izquierdaRetroceder(v);
}
void girarDerecha()
{
  if(estado == 'w')
  {
    izquierdaAvanzar(255);
    //derechaAvanzar(50);
    derechaParar();
  }
  else if(estado == 's')
  {
    derechaRetroceder(255);
    //izquierdaAvanzar(50);
    izquierdaParar();
  }
  else if(estado == 'p')
  {
    izquierdaAvanzar(255);
    derechaRetroceder(255);
  }
}
void girarIzquierda()
{
  if(estado == 'w')
  {
    derechaAvanzar(255);
    //izquierdaAvanzar(50);
    izquierdaParar();
  }
  else if(estado == 's')
  {
    izquierdaRetroceder(255);
    //derechaAvanzar(50);
    derechaParar();
  }
  else if(estado == 'p')
  {
    derechaAvanzar(255);
    izquierdaRetroceder(255);
  }
}
void pararMotor()
{
  estado = 'p';
  derechaParar();
  izquierdaParar();
}

void pararGiro()
{
  if(estado == 'w')
  {
    avanzar(velocidad);
  }
  else if(estado == 's')
  {
    retroceder(velocidad);
  }
  else if(estado == 'p')
  {
    pararMotor();
  }
}

void leerSensores()
{
   for(int i = 0; i < numSensores; i++)
   {
     distancia[i] = sensores[i].Ranging(CM);
   }
}

void imprimirSensores()
{
  Serial.print("distancia");
  Serial.print("\n");
   for(int i = 0; i < numSensores; i++)
   {
      Serial.print(distancia[i]);
      Serial.print(" ");
   }
   Serial.print("\n\n");
}

int distanciaMinima()
{
  leerSensores();
  int distanciaMenor = 52;
  int min = 6;
  for(int i = 0; i < numSensores; i++)
  {
    if(distancia[i] < distanciaMenor)
    {
      distanciaMenor = distancia[i];
      min = i;  
    }
  }
  return min;
}

void autonomo()
{
  int tiempo = 200;
  velocidad = 255;
  do
  {
    leerSensores();
    if(!detecta(distancia[0]) && !detecta(distancia[1]) && !detecta(distancia[2]))
    {
      avanzar(velocidad);
    }
    else
    {
      if(detecta(distancia[0]) && !detecta(distancia[2]))
      {
        pararMotor();
        pararGiro();
        girarDerecha();
        delay(tiempo);
      }
      else if(!detecta(distancia[0]) && detecta(distancia[2]))
      {
        pararMotor();
        pararGiro();
        girarIzquierda();
        delay(tiempo);
      }
      else
      {
        pararMotor();
        pararGiro();
        int ran = random(2);
        if(ran == 0)
        {
          girarDerecha();
        }
        else
        {
          girarIzquierda();
        }
        delay(tiempo);
      }
    }
  }while(Serial.read() != 'x');
  pararMotor();
  pararGiro();
}

bool detecta(int distancia)
{
  return distancia <= 25;
}

void seguir()
{
  velocidad = 255;
  int tiempo = 200;
  do
  {
    int sen = distanciaMinima();
    if(sen == 0)
    {
      pararMotor();
      girarIzquierda();
      delay(tiempo);
      pararGiro();
      avanzar(velocidad);
      delay(200);
    }
    else if(sen == 1)
    {
      avanzar(velocidad);
      delay(tiempo);
    }
    else if(sen == 2)
    {
      pararMotor();
      girarDerecha();
      delay(tiempo);
      pararGiro();
      avanzar(velocidad);
      delay(200);
    }
    else if(sen == 3)
    {
      pararMotor();
      girarDerecha();
      delay(tiempo);
      pararGiro();
      retroceder(velocidad);
      delay(200);
    }
    else if(sen == 4)
    {
      retroceder(velocidad);
      delay(tiempo);
    }
    else if(sen == 5)
    {
      pararMotor();
      girarIzquierda();
      delay(tiempo);
      pararGiro();
      retroceder(velocidad);
      delay(200);
    }
    else
    {
       pararGiro();
       pararMotor();
    }
  }while(Serial.read() != 'x');
  pararMotor();
  pararGiro();
}

void comprobarSensores()
{
  do
  {
    leerSensores();
    imprimirSensores();
    delay(500);
  }while(Serial.read() != 'x');
}

